package com.podbike.app.ui.about_device

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.podbike.app.R
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.databinding.FragmentAboutDeviceBinding
import com.podbike.app.ui.base.BaseFragment
import com.podbike.app.ui.base.adjustEdgeToEdgeMargins
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber.Forest.e
import javax.inject.Inject

@AndroidEntryPoint
class AboutDeviceFragment : BaseFragment() {

    private lateinit var binding: FragmentAboutDeviceBinding

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    var errorDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutDeviceBinding.inflate(inflater, container, false).apply {
            root.adjustEdgeToEdgeMargins()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBar()
        setupRecyclerView()
        lifecycleScope.launch {
            loadDeviceMetadata()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        errorDialog?.dismiss()
    }

    private fun setupAppBar() {
        binding.appBar.appBarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBar.appBarTitle.text = getString(R.string.AboutDevice)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = AboutDeviceAdapter(emptyList())
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadDeviceMetadata() {
        if (bluetoothManager.selectedDevice?.client?.isConnected != true) {
            showError()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        val device = bluetoothManager.selectedDevice ?: return showError()
        val deviceMetadata = try {
            device.data.getDeviceMetadata()
        } catch (e: Exception) {
            showError()
            e(e)
            return
        } ?: return showError()

        val items = mutableListOf<AboutDeviceItem>()

        items.add(
            AboutDeviceItem(
                "${getString(R.string.AboutDeviceProductId)}: ${deviceMetadata.productId}",
                false
            )
        )
        items.add(
            AboutDeviceItem(
                "${getString(R.string.AboutDeviceFrameNumber)}: ${deviceMetadata.frameNumber}",
                true
            )
        )

        deviceMetadata.ecuModules.forEach { ecoModule ->
            items.add(
                AboutDeviceItem(
                    "${getString(R.string.AboutDeviceBoardName)}: ${ecoModule.boardName}",
                    false
                )
            )
            items.add(
                AboutDeviceItem(
                    "${getString(R.string.AboutDeviceBoardId)}: ${ecoModule.boardId}",
                    false
                )
            )
            items.add(
                AboutDeviceItem(
                    "${getString(R.string.AboutDeviceBoardPosition)}: ${ecoModule.boardPosition}",
                    false
                )
            )
            items.add(
                AboutDeviceItem(
                    "${getString(R.string.AboutDeviceFirmwareVersion)}: ${ecoModule.fwVersion}",
                    false
                )
            )
            items.add(
                AboutDeviceItem(
                    "${getString(R.string.AboutDeviceSerialNumber)}: ${ecoModule.serialNumber}",
                    true
                )
            )
        }
        val adapter = AboutDeviceAdapter(items)
        binding.recyclerView.adapter = adapter
        binding.progressBar.visibility = View.GONE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        showAlertDialog(getString(R.string.UpdateIssue)) {
            findNavController().popBackStack(R.id.settingsFragment, false)
        }
    }

}