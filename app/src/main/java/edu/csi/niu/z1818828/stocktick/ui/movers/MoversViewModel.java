package edu.csi.niu.z1818828.stocktick.ui.movers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MoversViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MoversViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}