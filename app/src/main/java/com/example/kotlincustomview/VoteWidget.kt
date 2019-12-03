package com.example.kotlincustomview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.vote_widget_layout.view.*

/**
 *
 * created by graysonzeng on 2019/11/28.
 * email: graysonzeng@futunn.com
 **/
class VoteWidget:RelativeLayout,VoteButton.VoteClickListener {
    companion object{
        const val TAG:String = "VoteWidget"
    }

    /**
     * 根布局
     */
    private var root:View? = null

    /**
     * 管理已投票界面的ViewList
     */
    private val votedViewList:ArrayList<View> = ArrayList()

    /**
     * 管理未投票界面的ViewList
     */
    private val unVotedViewList:ArrayList<View> = ArrayList()

    /**
     * 填入的视图数据
     */
    private var mVoteWidgetDisplaySource:VoteWidgetDisplaySource? =null
//    private var mLeftTitle:String = "不看好"
//    private var mRightTitle:String = "看好"
//    private var mLeftNumber:Int = 0
//    private var mRightNumber:Int = 0
//    private var mDeadline:Long = 0
//    private var isPermanent:Boolean = true
//    private var isVoted:Boolean = true

    fun fill(displaySource:VoteWidgetDisplaySource){
        mVoteWidgetDisplaySource = displaySource
        if(mVoteWidgetDisplaySource == null){
            root?.visibility = View.GONE
            //loadData()
        }else{
            root?.visibility = View.VISIBLE
            updateUI(displaySource)
        }
    }

    /**
     * 构造器
     */
    constructor(context:Context):super(context){
        init(null, 0)
    }

    constructor(context: Context,attrs: AttributeSet):super(context,attrs){
        init(attrs,0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle:Int):super(context,attrs,defStyle){
        init(attrs,defStyle)
    }



    private fun init(attrs:AttributeSet?,defStyle:Int){
        //取出attrs, 为其中的View的值赋值等
        val a: TypedArray = context.obtainStyledAttributes(attrs,R.styleable.VoteWidget,defStyle,0)

        val textColor = a.getColor(R.styleable.VoteWidget_textColor, Color.WHITE)
        val leftColor = a.getColor(R.styleable.VoteWidget_leftColor,Color.BLUE)
        val rightColor = a.getColor(R.styleable.VoteWidget_rightColor,Color.RED)
        val buttonSlashUnderWidth = a.getDimension(R.styleable.VoteWidget_buttonSlashUnderWidth,20F)
        val buttonSlashWidth = a.getDimension(R.styleable.VoteWidget_buttonSlashWidth,5F)
        val barSlashUnderWidth = a.getDimension(R.styleable.VoteWidget_barSlashUnderWidth,10F)
        val barSlashWidth = a.getDimension(R.styleable.VoteWidget_barSlashWidth,3F)
        a.recycle()

        root = LayoutInflater.from(context).inflate(R.layout.vote_widget_layout,this,true)

        //填入voteButton的初始属性
        val voteButtonDisplaySource = VoteButtonDisplaySource(leftColor,rightColor,textColor,buttonSlashUnderWidth,buttonSlashWidth)
        voteButton.fill(voteButtonDisplaySource)
        val doubleMoveBarDisplaySource = DoubleMoveBarDisplaySource(leftColor,rightColor,barSlashUnderWidth,barSlashWidth)
        voteDoubleBar.fill(doubleMoveBarDisplaySource)
        //填入已投票页面左右文字的初始属性
        leftTitleTv.setTextColor(leftColor)
        leftPercentTv.setTextColor(leftColor)
        rightTitleTv.setTextColor(rightColor)
        rightPercentTv.setTextColor(rightColor)

        //设置vote_button的左右点击监听器
        voteButton.voteClickListener = this

        //把view加入View管理显示或者不显示
        votedViewList.add(leftTitleTv)
        votedViewList.add(leftIconView)
        votedViewList.add(rightIconView)
        votedViewList.add(rightTitleTv)
        votedViewList.add(linearBarAndText)
        unVotedViewList.add(voteButton)
    }

    /**
     * 更新UI 填充数据
     */
    private fun updateUI(displaySource: VoteWidgetDisplaySource){
        val (mLeftTitle,mRightTitle,mLeftNumber,mRightNumber,mDeadline,isPermanent,isVoted,isLeft) = displaySource
        voteButton.fill(mLeftTitle,mRightTitle)
        voteDoubleBar.fill(mLeftNumber,mRightNumber)
        deadlineTv.text = formatCountDownForSnsVote(mDeadline)
        if(isLeft){
            leftTitleTv.text = "已选「$mLeftTitle」"
            rightTitleTv.text = mRightTitle
        }else{
            leftTitleTv.text = mLeftTitle
            rightTitleTv.text = "已选「$mRightTitle」"
        }
        val leftPercent = computeLeftPercent(mLeftNumber,mRightNumber)
        leftPercentTv.text = "$leftPercent%"
        rightPercentTv.text = "${100-leftPercent}%"
        setVotedVisibility(isVoted)
        setDeadlineViewVisibility(isPermanent)
    }

    /**
     * 计算左右的percent
     */
    private fun computeLeftPercent(leftNo:Int,rightNo:Int):Int{
        val half:Float = 1F/2F
        var res = 0
        if((leftNo == 0) && (rightNo == 0)){
            res = (half*100).toInt()
        }else if(leftNo == 0 && rightNo != 0){
            res = 0
        }else if(rightNo == 0 && leftNo != 0){
            res = 100
        }else{
            res = (100 * leftNo/(leftNo+rightNo).toFloat()).toInt()
        }
        return res
    }


    /**
     * 设置已投票和未投票的情况下View的可见性
     */
    private fun setVotedVisibility(isVoted:Boolean){
        if(!isVoted){
            for(view in votedViewList){
                view.visibility = View.GONE
            }
            for(view in unVotedViewList){
                view.visibility = View.VISIBLE
            }
        }else{
            for(view in votedViewList){
                view.visibility = View.VISIBLE
            }
            for(view in unVotedViewList){
                view.visibility = View.GONE
            }
        }
    }

    /**
     *设置剩余天数的TextView的可见性
     */
    private fun setDeadlineViewVisibility(isPermanent: Boolean){
        if(isPermanent){
            deadlineTv.visibility = View.GONE
        }else{
            deadlineTv.visibility = View.VISIBLE
        }
    }

    /**
     * 在Detach的时候的释放资源
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        votedViewList.clear()
        unVotedViewList.clear()
    }

    /**
     * 重写左边的点击事件
     */
    override fun onClickLeft(){
        // TODO
    }

    /**
     * 重写右边的点击事件
     */
    override fun onClickRight(){
        // TODO
    }


    /**
     * 牛牛投票计算剩余时间
     */
    fun formatCountDownForSnsVote(countDownTime: Long): String? { // 倒计时时间比现在晚，则该阶段的倒计时已结束
        if (countDownTime <= 0) {
            return "投票已结束"
        }
        val ss = 1
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        val day = countDownTime / dd
        val hour = (countDownTime - day * dd) / hh
        val minute = (countDownTime - day * dd - hour * hh) / mi
        val second = (countDownTime - day * dd - hour * hh - minute * mi) / ss
        return if (day >= 90) {
            "剩余时间大于90天"
        } else if (day >= 1) {
            "剩余${day}天"
        } else if (hour >= 1) {
            "剩余${hour}小时"
        } else {
            if (minute < 1 && second != 0L) {
                "剩余1分钟"
            } else {
                "剩余${minute}分钟"
            }
        }
    }

}

//选择左边 isLeft为true
data class VoteWidgetDisplaySource(var mLeftTitle:String, var mRightTitle:String, var mLeftNumber:Int, var mRightNumber:Int, var mDeadline:Long, var isPermanent:Boolean, var isVoted:Boolean,var isLeft:Boolean)
