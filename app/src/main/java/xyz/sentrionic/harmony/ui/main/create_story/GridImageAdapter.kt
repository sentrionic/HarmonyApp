package xyz.sentrionic.harmony.ui.main.create_story

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.RequestManager
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.util.SquareImageView
import java.util.*

class GridImageAdapter(
    context: Context,
    private val layoutResource: Int,
    imgURLs: ArrayList<String>,
    private val requestManager: RequestManager
) : ArrayAdapter<String>(context, layoutResource, imgURLs) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class ViewHolder {
        internal var image: SquareImageView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val holder: ViewHolder
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false)
            holder = ViewHolder()
            holder.image = convertView!!.findViewById(R.id.gridImageView)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val imgURL = getItem(position)

        requestManager.load(imgURL).into(holder.image!!)

        return convertView
    }
}
