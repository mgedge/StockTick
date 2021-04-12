package edu.csi.niu.z1818828.stocktick.ui.stock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StockViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StockViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}