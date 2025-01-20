package com.podbike.app.ui.firmware_update

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.UserPreferences
import com.podbike.app.data.api.model.FirmwareFilesData
import com.podbike.app.data.api.model.OtaFile
import com.podbike.app.data.api.model.OtaFileType
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.model.AudioFile
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferState
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
import com.podbike.app.data.bluetooth.model.PodbikeDeviceMetadata
import com.podbike.app.data.bluetooth.model.PodbikeTransferConfig
import com.podbike.app.data.bluetooth.model.SupportedBoard
import com.podbike.app.data.bluetooth.utils.YModem
import com.podbike.app.data.repository.FirmwareRepository
import com.podbike.app.ui.base.StateViewModel
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.ErrorTypeSealed.CheckForUpdatesError
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.ErrorTypeSealed.GetLicenceError
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.ErrorTypeSealed.LoadFirmwareFilesError
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.ErrorTypeSealed.NoBluetoothDeviceError
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.FirmwareUpdateAction
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.FirmwareUpdateEffect
import com.podbike.app.ui.firmware_update.FirmwareUpdateViewModel.FirmwareUpdateState
import com.podbike.app.ui.firmware_update.FirmwareVersion.*
import com.podbike.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest.i
import javax.inject.Inject

@HiltViewModel
class FirmwareUpdateViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val firmwareRepository: FirmwareRepository,
    private val userPreferences: UserPreferences
) : StateViewModel<FirmwareUpdateState, FirmwareUpdateAction, FirmwareUpdateEffect>(
    FirmwareUpdateState()
) {

    sealed class ErrorTypeSealed(val error: Throwable) {
        class CheckForUpdatesError(error: Throwable) : ErrorTypeSealed(error)
        class GetLicenceError(error: Throwable) : ErrorTypeSealed(error)
        class LoadFirmwareFilesError(error: Throwable) : ErrorTypeSealed(error)
        class NoBluetoothDeviceError(error: Throwable) : ErrorTypeSealed(error)
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            if (uiState.value.isBluetoothEnabled == false) {
                setErrorState(NoBluetoothDeviceError(Throwable("Bluetooth not available")))
                return@launch
            }


            val deviceMetadata = getDeviceMetadata()
            if (deviceMetadata == null) {
                setErrorState(CheckForUpdatesError(Throwable("No device metadata")))
                return@launch
            }
            val isUpToDate = firmwareRepository.checkIsUpToDate(deviceMetadata)
            i("isUpToDate: $isUpToDate")

            when (isUpToDate) {
                is Result.Error -> setErrorState(CheckForUpdatesError(isUpToDate.error))
                is Result.Success -> {
                    when (isUpToDate.data) {
                        true -> setFirmwareVersionState(UP_TO_DATE)
                        false -> setFirmwareVersionState(UPDATE_AVAILABLE)
                    }
                }
            }
        }
    }

    private fun setErrorState(error: ErrorTypeSealed) {
        updateState {
            copy(
                firmwareVersion = FirmwareVersion.ERROR,
                error = error,
                uploadingFileCount = null,
                firmwareFileTransferStatus = null,
                isSendingFiles = false
            )
        }
    }

    private fun getAndTransferFirmwareFiles() {
        viewModelScope.launch {
            val frameNumber = getFrameNumber()
            if (frameNumber == null) {
                setErrorState(CheckForUpdatesError(Throwable("No frame number")))
                return@launch
            }
            val firmwareFilesData = firmwareRepository.getFirmwareFilesList(frameNumber)
            i("firmwareFilesData: $firmwareFilesData")
            when (firmwareFilesData) {
                is Result.Error -> setErrorState(LoadFirmwareFilesError(firmwareFilesData.error))
                is Result.Success -> {
                    if (firmwareFilesData.data == null) {
                        setErrorState(LoadFirmwareFilesError(Throwable("No firmware files data")))
                        return@launch
                    }

                    val updateFiles = mutableListOf<OtaFile>()
                    updateFiles.add(createFrikarTransferConfigFile(firmwareFilesData.data))
                    firmwareFilesData.data.firmwareModuleByUpdateId.forEach { moduleData ->
                        updateFiles.add(
                            OtaFile(
                                type = OtaFileType.FIRMWARE,
                                name = moduleData.fileName,
                                bytes = null
                            )
                        )
                    }

                    val totalFileCount = updateFiles.size

                    updateFiles
                        .forEachIndexed { index, otaFile ->
                            i("otaFile: $otaFile")
                            updateState {
                                copy(
                                    uploadingFileCount = index + 1,
                                    totalFileCount = totalFileCount,
                                    isSendingFiles = true
                                )
                            }
                            transferOtaFile(otaFile)
                            if (uiState.value.error != null) {
                                setErrorState(LoadFirmwareFilesError(Throwable("Error transferring files")))
                                return@launch
                            }
                        }
                    updateState { copy(isSendingFiles = false) }
                    setFirmwareVersionState(TRANSFER_COMPLETED)
                }
            }
        }
    }

    private suspend fun transferOtaFile(otaFile: OtaFile) {
        val firmwareFileName = otaFile.name
        when (otaFile.type) {
            OtaFileType.FRIKAR_TRANSFER_CONFIG -> {
                bluetoothManager.transferFileToDevice(otaFile)
                    ?.collect {
                        updateState {
                            copy(
                                firmwareFileTransferStatus = it,
                                isLoading = false,
                                error = if (it.status == FirmwareFileTransferState.FAILED) {
                                    LoadFirmwareFilesError(Throwable("File transfer failed"))
                                } else null
                            )
                        }
                    }
            }

            OtaFileType.FIRMWARE, OtaFileType.AUDIO -> {
                when (val firmwareUpdateFile =
                    firmwareRepository.getFirmwareFile(firmwareFileName)) {
                    is Result.Error -> setErrorState(
                        LoadFirmwareFilesError(
                            firmwareUpdateFile.error
                        )
                    )

                    is Result.Success -> {
                        bluetoothManager.transferFileToDevice(firmwareUpdateFile.data!!)
                            ?.collect {
                                updateState {
                                    copy(
                                        firmwareFileTransferStatus = it,
                                        isLoading = false,
                                        error = if (it.status == FirmwareFileTransferState.FAILED) {
                                            LoadFirmwareFilesError(Throwable("File transfer failed"))
                                        } else null
                                    )
                                }
                            }
                    }
                }

            }
        }
    }

    override fun processAction(action: FirmwareUpdateAction) {
        when (action) {
            is FirmwareUpdateAction.ForwardAction -> {
                if (action.isBluetoothEnabled == false) {
                    updateState {
                        copy(
                            isLoading = false,
                            error = ErrorTypeSealed.NoBluetoothDeviceError(Throwable("Bluetooth not available"))
                        )
                    }
                    return
                }
                when (uiState.value.firmwareVersion) {
                    UNKNOWN -> {
                        setFirmwareVersionState(ERROR)
                    }

                    UP_TO_DATE -> {
                        checkForUpdates()
                    }

                    UPDATE_AVAILABLE -> {
                        viewModelScope.launch {
                            val license = firmwareRepository.getLicense()
                            when (license) {
                                is Result.Error -> {
                                    setErrorState(GetLicenceError(license.error))
                                }

                                is Result.Success -> {
                                    updateState {
                                        copy(
                                            firmwareVersion = LICENSE_AGREEMENT,
                                            firmwareLicense = license.data,
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LICENSE_AGREEMENT -> {
                        setFirmwareVersionState(TRANSFER_STARTED)
                        getAndTransferFirmwareFiles()
//                        setFirmwareVersionState(TRANSFER_COMPLETED)

                    }

                    TRANSFER_STARTED -> {
                        setFirmwareVersionState(TRANSFER_COMPLETED)
                    }

                    TRANSFER_COMPLETED -> {
                        setFirmwareVersionState(UPGRADE)
                        viewModelScope.launch {
                            val device =
                                bluetoothManager.selectedDevice ?: return@launch setErrorState(
                                    NoBluetoothDeviceError(
                                        Throwable("No selected device")
                                    )
                                )
                            YModem.YModemHelper.runUpgrade(device)
                            val config = bluetoothManager.selectedDevice?.data?.getDeviceMetadata()
                            userPreferences.setUpdateStartedFlag(true, config)
                        }
                    }

                    UPGRADE -> {
                        println("TRANSFER_STARTED")
                        //no action
                    }

                    ERROR -> {
                        checkForUpdates()
                    }
                }
            }

            is FirmwareUpdateAction.BluetoothPermissions -> {
                sendEffect(FirmwareUpdateEffect.NavigateToBluetoothPermissions)
            }

            is FirmwareUpdateAction.LocationPermissions -> {
                sendEffect(FirmwareUpdateEffect.NavigateToLocationPermissions)
            }

            is FirmwareUpdateAction.EnableBluetooth -> {
                sendEffect(FirmwareUpdateEffect.NavigateToBluetoothSettings)
            }

            is FirmwareUpdateAction.EnableLocation -> {
                sendEffect(FirmwareUpdateEffect.NavigateToLocationSettings)
            }

            is FirmwareUpdateAction.PermissionsChanged -> {
                val hasAllPermissions =
                    action.hasBluetoothPermissions && action.isBluetoothEnabled && action.isLocationEnabled
                val hasStartedFirmwareUpdate = uiState.value.firmwareVersion != UNKNOWN
                Timber.tag("FirmwareUpdateViewModel")
                    .d("PermissionsChanged: $action" + " firmwareVersion: ${uiState.value.firmwareVersion}")
                updateState {
                    copy(
                        hasBluetoothPermissions = action.hasBluetoothPermissions,
                        isBluetoothEnabled = action.isBluetoothEnabled,
                        isLocationEnabled = action.isLocationEnabled,
                        isLoading = hasAllPermissions,
                    )
                }
                if (hasAllPermissions && !hasStartedFirmwareUpdate) {
                    Timber.tag("FirmwareUpdateViewModel").d("checkForUpdates")
                    checkForUpdates()
                } else {
                    setErrorState(NoBluetoothDeviceError(Throwable("Bluetooth not available")))
                }
            }

            is FirmwareUpdateAction.Retry -> {
                updateState { copy(isLoading = true, error = null) }
            }

            is FirmwareUpdateAction.GoBack -> {
                sendEffect(FirmwareUpdateEffect.NavigateBack)
            }
        }
    }

    private fun createFrikarTransferConfigFile(serverFirmwareModules: FirmwareFilesData): OtaFile {
        val supportedBoards = serverFirmwareModules.firmwareModuleByUpdateId
            .filter { it.serialNumber != null }
            .map { serverFirmwareModule ->
                val boardName = serverFirmwareModule.fileName
                    .split(":").lastOrNull()
                    ?.split(".")?.firstOrNull() ?: ""
                SupportedBoard(
                    boardName = boardName,
                    boardId = serverFirmwareModule.boardName,
                    serialNumber = serverFirmwareModule.serialNumber.toString(),
                    firmwareVersion = serverFirmwareModule.firmwareVersion,
                    fileName = serverFirmwareModule.fileName
                )
            }

        val audioFiles = serverFirmwareModules.firmwareModuleByUpdateId
            .map { AudioFile(it.fileName) }

        val frikarTransferConfig = PodbikeTransferConfig(
            productName = "FRIKAR",
            releaseId = serverFirmwareModules.releaseId,
            productId = serverFirmwareModules.productId,
            supportedBoards = supportedBoards,
            audioFiles = audioFiles
        )

        val configData = Gson().toJson(frikarTransferConfig).toByteArray()

        return OtaFile(
            type = OtaFileType.FRIKAR_TRANSFER_CONFIG,
            name = "frikar.json",
            bytes = configData
        )
    }

    private fun setFirmwareVersionState(firmwareVersion: FirmwareVersion) {
        updateState {
            copy(
                firmwareVersion = firmwareVersion,
                isLoading = false,
                error = null
            )
        }
    }

    private var deviceMetadata: PodbikeDeviceMetadata? = null
    private suspend fun getDeviceMetadata(): PodbikeDeviceMetadata? {
        if (deviceMetadata != null) return deviceMetadata
        deviceMetadata = bluetoothManager.selectedDevice?.data?.getDeviceMetadata()
        return deviceMetadata
    }

    private suspend fun getFrameNumber(): String? {
//        //TODO remove mock
//        return "000000-F8-1-00-000"
        return getDeviceMetadata()?.frameNumber
    }

    data class FirmwareUpdateState(
        val hasBluetoothPermissions: Boolean = false,
        val isBluetoothEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,
        val isLoading: Boolean = true,
        val firmwareVersion: FirmwareVersion = FirmwareVersion.UNKNOWN,
        val firmwareLicense: String? = null,
        val firmwareFileTransferStatus: FirmwareFileTransferStatus? = null,
        val uploadingFileCount: Int? = null,
        val totalFileCount: Int? = null,
        val isSendingFiles: Boolean = false,
        val error: ErrorTypeSealed? = null
    ) : UiState

    sealed class FirmwareUpdateAction : UiAction {
        data class ForwardAction(
            val isBluetoothEnabled: Boolean
        ) : FirmwareUpdateAction()

        data object BluetoothPermissions : FirmwareUpdateAction()
        data object LocationPermissions : FirmwareUpdateAction()
        data object EnableBluetooth : FirmwareUpdateAction()
        data object EnableLocation : FirmwareUpdateAction()
        data class PermissionsChanged(
            val hasBluetoothPermissions: Boolean = false,
            val isBluetoothEnabled: Boolean = false,
            val isLocationEnabled: Boolean = false,
        ) : FirmwareUpdateAction()

        data object Retry : FirmwareUpdateAction()
        data object GoBack : FirmwareUpdateAction()
    }

    sealed class FirmwareUpdateEffect : UiEffect {
        data object NavigateBack : FirmwareUpdateEffect()
        data object NavigateToBluetoothPermissions : FirmwareUpdateEffect()
        data object NavigateToLocationPermissions : FirmwareUpdateEffect()
        data object NavigateToBluetoothSettings : FirmwareUpdateEffect()
        data object NavigateToLocationSettings : FirmwareUpdateEffect()
        data object NavigateToDashboard : FirmwareUpdateEffect()
    }

}