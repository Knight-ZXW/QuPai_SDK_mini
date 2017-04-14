package com.duanqu.qupaicustomuidemo.editor.manager;

/**
 * Created by Administrator on 2016/11/22.
 */
public interface MultiSelectedToCheck<T> {

    void toggleMultiCheckMode(boolean start);

    void removeSelectedItem();

    T[] getSelectedItems();

    void selectedAllItems();

    void unselectedAllItems();

    void selectedItem(T item);

    void unselectedItem(T item);

    boolean isItemSelected(T item);

}
