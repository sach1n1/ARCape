package com.example.arcape
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator


class Onboarding : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.onboarding)
         val toolbar = findViewById<Toolbar>(R.id.toolbar)
         setSupportActionBar(toolbar)
         val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)
         viewPager = findViewById(R.id.viewpager)
         val startBtn = findViewById<Button>(R.id.startBtn)
         viewPagerAdapter = ViewPagerAdapter(this)
         viewPager.adapter = viewPagerAdapter
         viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
             override fun onPageScrolled(
                 position: Int,
                 positionOffset: Float,
                 positionOffsetPixels: Int
             ) {}

             override fun onPageSelected(position: Int) {
                 if(position==3)
                 {
                     startBtn.visibility= View.VISIBLE
                 }
             }

             override fun onPageScrollStateChanged(state: Int) {}
         })
        dotsIndicator.setViewPager(viewPager)
        startBtn.setOnClickListener {
            val startAR = Intent(
                this,
                Number::class.java
            )
            startActivity(startAR)
            this.finish()
        }
     }
}