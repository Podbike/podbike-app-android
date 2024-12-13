package com.podbike.app.ui.firmware_update

import androidx.lifecycle.viewModelScope
import com.kfc_polska.ui.base.UiAction
import com.kfc_polska.ui.base.UiEffect
import com.kfc_polska.ui.base.UiState
import com.podbike.app.data.api.model.FirmwareModuleData
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferState
import com.podbike.app.data.bluetooth.model.FirmwareFileTransferStatus
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest.i
import javax.inject.Inject

@HiltViewModel
class FirmwareUpdateViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val firmwareRepository: FirmwareRepository
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


            val frameNumber = getFrameNumber()
            if (frameNumber == null) {
                setErrorState(CheckForUpdatesError(Throwable("No frame number")))
                return@launch
            }
            val isUpToDate = firmwareRepository.checkIsUpToDate(frameNumber)
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
                uploadingFileIndex = null,
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
                    //@TODO: UNCOMMENT
                    val totalFileCount =
                        firmwareFilesData.data?.firmwareModuleByUpdateId.orEmpty().size
                    firmwareFilesData.data?.firmwareModuleByUpdateId.orEmpty()
                        .forEachIndexed { index, firmwareModule ->
                            i("firmwareModule: $firmwareModule")
                            updateState {
                                copy(
                                    uploadingFileIndex = index,
                                    totalFileCount = totalFileCount,
                                    isSendingFiles = true
                                )
                            }
                            transferFirmwareFile(firmwareModule)
                        }
//                    transferFirmwareFile(firmwareFilesData.data?.firmwareModuleByUpdateId?.firstOrNull())
                    updateState { copy(isSendingFiles = false) }
                    setFirmwareVersionState(TRANSFER_COMPLETED)
                }
            }
        }
    }

    private suspend fun transferFirmwareFile(firmwareModule: FirmwareModuleData?) {
        val firmwareFileName = firmwareModule?.fileName
        if (firmwareFileName == null) {
            setErrorState(LoadFirmwareFilesError(Throwable("No firmware file name")))
            return
        }
        when (val firmwareUpdateFile = firmwareRepository.getFirmwareFile(firmwareFileName)) {
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
                                error = null
                            )
                        }
                        println("currentPackage: ${it.currentPackage}, totalPackages: ${it.totalPackages}")
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
                    }

                    TRANSFER_STARTED -> {
                        setFirmwareVersionState(TRANSFER_COMPLETED)
                    }

                    TRANSFER_COMPLETED -> {
                        setFirmwareVersionState(UPGRADE)
                        viewModelScope.launch {
                            println("UPGRADE")
                            delay(3000)
                            setFirmwareVersionState(UP_TO_DATE)
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

    private fun setFirmwareVersionState(firmwareVersion: FirmwareVersion) {
        updateState {
            copy(
                firmwareVersion = firmwareVersion,
                isLoading = false,
                error = null
            )
        }
    }

    private fun getFrameNumber(): String? {
//        //TODO remove mock
//        return "000000-F8-1-00-000"
        return bluetoothManager.selectedDevice?.data?.deviceMetadata?.frameNumber
    }

    data class FirmwareUpdateState(
        val hasBluetoothPermissions: Boolean = false,
        val isBluetoothEnabled: Boolean = false,
        val isLocationEnabled: Boolean = false,
        val isLoading: Boolean = true,
        val firmwareVersion: FirmwareVersion = FirmwareVersion.UNKNOWN,
        val firmwareLicense: String? = null,
        val firmwareFileTransferStatus: FirmwareFileTransferStatus? = null,
        val uploadingFileIndex: Int? = null,
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
    }

}