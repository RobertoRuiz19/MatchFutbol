package com.example.futbolmatch.ui.maps

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.futbolmatch.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var equipo: String = ""

    companion object {
        private const val ARG_LATITUD = "latitud"
        private const val ARG_LONGITUD = "longitud"
        private const val ARG_EQUIPO = "equipo"


        fun newInstance(latitud: Double, longitud: Double, equipo: String): MapsFragment {
            val fragment = MapsFragment()
            val args = Bundle()
            args.putDouble(ARG_LATITUD, latitud)
            args.putDouble(ARG_LONGITUD, longitud)
            args.putString(ARG_EQUIPO, equipo)
            fragment.arguments = args
            return fragment
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        // Manipula el mapa una vez que esté disponible
        val location = LatLng(latitud, longitud)
        googleMap.addMarker(MarkerOptions().position(location).title(equipo))

        // Establecer zoom al 70%
        val zoomLevel = 15f
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        // Botón de retroceso
        val button = view.findViewById<ImageButton>(R.id.buttonMapBack)
        button.setOnClickListener { view -> back() }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            latitud = it.getDouble(ARG_LATITUD)
            longitud = it.getDouble(ARG_LONGITUD)
            equipo = it.getString(ARG_EQUIPO).toString()
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    fun back() {
        val navController = findNavController()
        navController.popBackStack()
    }


}
