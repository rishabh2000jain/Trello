package com.example.trello.commomWidgets

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.example.trello.R
import com.example.trello.databinding.CurvedButtonBinding

class CurvedButton(private val context: Context, private val attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {
        companion object{
             val TAG:String = CurvedButton::class.java.simpleName
        }
        private var curvedButtonBinding:CurvedButtonBinding

        private var isLoading:Boolean=false
        private var buttonTextSize:Int?=null
        private var buttonTextColor:Int?=null
        private var buttonText:String?=null
        private var buttonBgId:Int?=null
        private var buttonColor:Int?=null
        private var buttonLoaderColor:Int?=null

        init {
            val view = LayoutInflater.from(context).inflate(R.layout.curved_button,this,true)
            curvedButtonBinding = CurvedButtonBinding.bind(view)

            parseAttr()
            setupUI()
            setLoading(false)
        }


    private fun parseAttr(){
        val attribute: TypedArray =
            getContext().obtainStyledAttributes(attributeSet, R.styleable.CurvedButton)

        try {
              buttonColor = attribute.getInt(
                R.styleable.CurvedButton_buttonBackgroundColor,
                resources.getColor(android.R.color.white)
            )
            attribute.getColor(R.styleable.CurvedButton_buttonBackgroundColor, 0)
                .also { if (it != 0) buttonColor = it }
            buttonBgId = attribute.getResourceId(
                R.styleable.CurvedButton_cbButtonBackground,-9)
            buttonText = attribute.getString(R.styleable.CurvedButton_cbButtonTitle).toString()
            buttonTextColor = attribute.getInt(
                R.styleable.CurvedButton_buttonTextColor,
                resources.getColor(android.R.color.white)
            )
            attribute.getColor(R.styleable.CurvedButton_buttonTextColor, 0)
                .also { if (it != 0) buttonTextColor = it }
            buttonTextSize = attribute.getDimensionPixelSize(R.styleable.CurvedButton_buttonFontSize, 10)


            buttonLoaderColor = attribute.getInt(
                R.styleable.CurvedButton_loaderColor,
                resources.getColor(android.R.color.white)
            )
            attribute.getColor(R.styleable.CurvedButton_loaderColor, 0)
                .also { if (it != 0) buttonLoaderColor = it }
        }catch (exception:java.lang.Exception){
            Log.d(TAG,exception.toString())
        }finally {
            attribute.recycle()
        }

    }

    private fun setupUI(){
        val progressbarDrawable = curvedButtonBinding.loader.indeterminateDrawable?.mutate()
        progressbarDrawable?.colorFilter = buttonLoaderColor?.let {
            PorterDuffColorFilter(
                it, PorterDuff.Mode.SRC_IN
            )
        }
        curvedButtonBinding.loader.progressDrawable = progressbarDrawable
        curvedButtonBinding.buttonTxt.textSize = buttonTextSize?.toFloat() ?: 10.0f
        curvedButtonBinding.buttonTxt.text = buttonText
        buttonTextColor?.let { curvedButtonBinding.buttonTxt.setTextColor(it) }
        buttonBgId?.let {
            try{
                curvedButtonBinding.root.background = ContextCompat.getDrawable(context,it)
            }catch (exception:NotFoundException){
                Log.d(TAG,exception.toString())
            }
        }

    }

    fun getLoading() = isLoading

    fun setLoading(loading:Boolean){
        isLoading = loading
        if(loading){
            curvedButtonBinding.loader.visibility = View.VISIBLE
            curvedButtonBinding.buttonTxt.visibility = View.GONE
        }else{
            curvedButtonBinding.loader.visibility = View.GONE
            curvedButtonBinding.buttonTxt.visibility = View.VISIBLE

        }
    }

    fun setText(text:String){
        curvedButtonBinding.buttonTxt.text = text
    }

}