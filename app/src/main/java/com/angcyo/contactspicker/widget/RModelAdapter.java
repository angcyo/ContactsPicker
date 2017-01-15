package com.angcyo.contactspicker.widget;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;

import com.angcyo.contactspicker.util.Reflect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * 支持单选, 多选的Adapter
 * <p>
 * Created by angcyo on 2016-12-19.
 */

public abstract class RModelAdapter<T> extends RBaseAdapter<T> {
    /**
     * 正常
     */
    public static final int MODEL_NORMAL = 1;
    /**
     * 单选
     */
    public static final int MODEL_SINGLE = MODEL_NORMAL << 1;
    /**
     * 多选
     */
    public static final int MODEL_MULTI = MODEL_SINGLE << 1;
    private HashSet<Integer> mSelector = new HashSet<>();
    @Model
    private int mModel = MODEL_NORMAL;

    private HashSet<OnModelChangeListener> mChangeListeners = new HashSet<>();

    public RModelAdapter(Context context) {
        super(context);
    }

    public RModelAdapter(Context context, List<T> datas) {
        super(context, datas);
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return 0;
    }

    @Override
    protected void onBindView(RBaseViewHolder holder, int position, T bean) {
        onBindCommonView(holder, position, bean);
        if (mModel == MODEL_NORMAL) {
            onBindNormalView(holder, position, bean);
        } else {
            onBindModelView(mModel, isPositionSelector(position), holder, position, bean);
        }
    }

    /**
     * 公共布局部分
     */
    protected abstract void onBindCommonView(RBaseViewHolder holder, int position, T bean);

    protected abstract void onBindModelView(int model, boolean isSelector, RBaseViewHolder holder, int position, T bean);

    protected abstract void onBindNormalView(RBaseViewHolder holder, int position, T bean);

    /**
     * 在单选模式下, 选择其他项时, 将要先取消之前的选中项. 此时会执行此方法, 取消之前按钮的状态
     */
    protected void onUnselectorPosition(List<Integer> list) {

    }

    /**
     * 在执行 {@link #onUnselectorPosition(List)}后, 调用此方法, 可以便捷的取消 CompoundButton 的状态
     */
    public void unselector(@NonNull List<Integer> list, @NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        for (Integer pos : list) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.tag(viewTag);
                if (view != null && view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, false);
                }
            }
        }
    }

    /**
     * 在执行 {@link #onUnselectorPosition(List)}后, 调用此方法, 可以便捷的取消 CompoundButton 的状态
     */
    public void unselector(@NonNull List<Integer> list, @NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        for (Integer pos : list) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.v(viewId);
                if (view != null && view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, false);
                }
            }
        }
    }

    /**
     * 取消所有选择
     */
    public void unselectorAll(@NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.v(viewId);
                if (view != null && view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, false);
                }
            }
        }
        mSelector.clear();

        notifySelectorChange();
    }

    /**
     * 通知选中数量改变了
     */
    private void notifySelectorChange() {
        final Iterator<OnModelChangeListener> iterator = mChangeListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onSelectorChange(getAllSelectorList());
        }
    }

    /**
     * 取消所有选择
     */
    public void unselectorAll(@NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.tag(viewTag);
                if (view != null && view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, false);
                }
            }
        }
        mSelector.clear();

        notifySelectorChange();
    }

    /**
     * 互斥的操作
     */
    public void setSelectorPosition(int position) {
        setSelectorPosition(position, null);
    }

    /**
     * 选中所有
     */
    public void setSelectorAll(@NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        if (mModel != MODEL_MULTI) {
            return;
        }

        for (int i = 0; i < getAllDatas().size(); i++) {
            mSelector.add(i);
        }

        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.tag(viewTag);
                if (view != null && view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, true);
                }
            }
        }

        notifySelectorChange();
    }

    /**
     * 选中所有
     */
    public void setSelectorAll(@NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        if (mModel != MODEL_MULTI) {
            return;
        }

        for (int i = 0; i < getAllDatas().size(); i++) {
            mSelector.add(i);
        }


        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.v(viewId);
                if (view != null && view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, true);
                }
            }
        }

        notifySelectorChange();
    }

    public void addOnModelChangeListener(OnModelChangeListener listener) {
        mChangeListeners.add(listener);
    }

    public void removeOnModelChangeListener(OnModelChangeListener listener) {
        mChangeListeners.remove(listener);
    }

    public HashSet<Integer> getAllSelector() {
        return mSelector;
    }

    public List<Integer> getAllSelectorList() {
        List<Integer> integers = new ArrayList<>();
        final Iterator<Integer> iterator = mSelector.iterator();
        while (iterator.hasNext()) {
            integers.add(iterator.next());
        }
        return integers;
    }

    public void setSelectorPosition(int position, CompoundButton compoundButton) {
        final boolean selector = isPositionSelector(position);

        if (selector) {
            if (mModel == MODEL_SINGLE) {
                return;
            } else {
                mSelector.remove(position);
            }
        } else {
            if (mModel == MODEL_SINGLE) {
                onUnselectorPosition(getAllSelectorList());
                mSelector.clear();
            }
            mSelector.add(position);
        }

        notifySelectorChange();

        checkedButton(compoundButton, !selector);
    }

    private void checkedButton(CompoundButton compoundButton, boolean checked) {
        if (compoundButton == null) {
            return;
        }
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener =
                (CompoundButton.OnCheckedChangeListener) Reflect.getMember(CompoundButton.class,
                        compoundButton, "mOnCheckedChangeListener");
        compoundButton.setOnCheckedChangeListener(null);
        compoundButton.setChecked(checked);
        compoundButton.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    public boolean isPositionSelector(int position) {
        return mSelector.contains(position);
    }

    public int getModel() {
        return mModel;
    }

    /**
     * 设置模式,
     *
     * @param model 单选,多选, 正常
     */
    public void setModel(@Model int model) {
        if (mModel != model) {
            int old = mModel;
            mModel = model;

            final Iterator<OnModelChangeListener> iterator = mChangeListeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().onModelChange(old, model);
            }

            if (mModel != MODEL_NORMAL) {
                setEnableLoadMore(false);
                mSelector.clear();
            }
            notifyDataSetChanged();
        }
    }

    @IntDef({MODEL_NORMAL, MODEL_SINGLE, MODEL_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Model {
    }

    public interface OnModelChangeListener {
        void onModelChange(@Model int fromModel, @Model int toModel);

        void onSelectorChange(List<Integer> selectorList);
    }
}
