package com.github.godspeed010.weblib.adapters

interface ItemTouchHelperAdapter {
    //Called when an item has been dragged far enough to trigger a move
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}