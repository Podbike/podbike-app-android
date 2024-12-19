package com.podbike.app.ui.about_device

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.podbike.app.R
import com.podbike.app.data.bluetooth.manager.BluetoothManager
import com.podbike.app.databinding.FragmentAboutDeviceBinding
import com.podbike.app.ui.base.adjustEdgeToEdgeMargins
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AboutDeviceFragment : Fragment() {

    private lateinit var binding: FragmentAboutDeviceBinding

    @Inject
    lateinit var bluetoothManager: BluetoothManager

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
        binding.progressBar.visibility = View.VISIBLE
        val device = bluetoothManager.selectedDevice ?: return showError()
        val deviceMetadata = device.data.getDeviceMetadata() ?: return showError()

        val items = mutableListOf<AboutDeviceItem>()

        items.add(AboutDeviceItem("Product ID: ${deviceMetadata.productId}", false))
        items.add(AboutDeviceItem("Frame Number: ${deviceMetadata.frameNumber}", true))

        deviceMetadata.ecuModules.forEach { ecoModule ->
            items.add(AboutDeviceItem("Board Name: ${ecoModule.boardName}", false))
            items.add(AboutDeviceItem("Board ID: ${ecoModule.boardId}", false))
            items.add(AboutDeviceItem("Board Position: ${ecoModule.boardPosition}", false))
            items.add(AboutDeviceItem("Firmware Version: ${ecoModule.fwVersion}", false))
            items.add(AboutDeviceItem("Serial Number: ${ecoModule.serialNumber}", true))
        }
        val adapter = AboutDeviceAdapter(items)
        binding.recyclerView.adapter = adapter
        binding.progressBar.visibility = View.GONE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        showGenericErrorDialog()
    }

    private fun showGenericErrorDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setMessage(getString(R.string.UpdateIssue))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack(R.id.settingsFragment, false)
            }
            .setCancelable(false)
            .create()
            .show()
    }

}