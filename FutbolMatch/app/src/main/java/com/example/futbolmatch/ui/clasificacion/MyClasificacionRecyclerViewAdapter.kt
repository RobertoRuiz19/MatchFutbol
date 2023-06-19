import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.futbolmatch.R
import com.example.futbolmatch.modelo.dto.Clasificacion

class MyClasificacionRecyclerViewAdapter(
    var tabla: List<Clasificacion>,
    private val glide: RequestManager
) : RecyclerView.Adapter<MyClasificacionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_clasificacion_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tabla[position]

        glide.load(item.foto)
            .override(200, 200)
            .into(holder.foto)

        holder.pts.text = item.pts.toString()
        holder.ganados.text = item.pGanados.toString()
        holder.empatados.text = item.pEmpatados.toString()
        holder.perdidos.text = item.pPerdidos.toString()
    }

    override fun getItemCount(): Int {
        return tabla.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foto: ImageView = view.findViewById(R.id.equipo_escudo)
        val pts: TextView = view.findViewById(R.id.pts)
        val ganados: TextView = view.findViewById(R.id.ganados)
        val empatados: TextView = view.findViewById(R.id.empatados)
        val perdidos: TextView = view.findViewById(R.id.perdidos)
    }
}