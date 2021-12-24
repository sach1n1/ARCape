package com.example.arcape

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class ViewPagerAdapter(val context: Context): PagerAdapter() {
    var layoutInflater: LayoutInflater? = null

    //image array

    val imageArray = arrayOf(
        R.drawable.hello,
        R.drawable.control_room,
        R.drawable.scan
    )

    val headArray = arrayOf(
        "Hello Interns",
        "Welcome",
        "Scan Around"
    )

    val descriptionArray = arrayOf(
        "We are so excited to have you on our team!",
        "Here in the control room, you can see what's going on in the city",
        "Scan the images around the room, to understand their functionality"
    )

    override fun getCount(): Int {
        return headArray.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater!!.inflate(R.layout.slider, container, false)
        val image = view.findViewById<ImageView>(R.id.image)
        val headText = view.findViewById<TextView>(R.id.headText)
        val descriptionText = view.findViewById<TextView>(R.id.descriptionText)

        image.setImageResource(imageArray[position])
        headText.text = headArray[position]
        descriptionText.text = descriptionArray[position]

        container.addView(view)

        return view
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }
}