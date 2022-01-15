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
        R.drawable.puzzle,
        R.drawable.scan
    )

    val headArray = arrayOf(
        "Hello!!",
        "The task",
        "Scan Around"
    )

    val descriptionArray = arrayOf(
        "Welcome to the Smart Escape Room.",
        "Use your skills and solve the puzzles and get to the Control Room.",
        "Scan the images to get hints"
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