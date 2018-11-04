package com.shouxh.weatherMain;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends BaseAdapter implements Filterable {
    private List<String> list ;
    private Context context;
    private ContentFilter filter ;// 创建Filter对象
    private FilterListener listener ;// 接口对象

    public FilterAdapter(List<String> list, Context context, FilterListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder ;
        if (convertView == null) {
           convertView = LayoutInflater.from(context).inflate(R.layout.search_list, null,false);
            holder = new ViewHolder();
            holder.cityName =  convertView.findViewById(R.id.cityName);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.cityName.setText(list.get(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ContentFilter(list);
        }
        return filter;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    class ContentFilter extends Filter {
        // 创建集合保存原始数据
        private List<String> original;

        public ContentFilter(List<String> list) {
            this.original = list;
        }

        /**
         * 该方法返回搜索过滤后的数据
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // 创建FilterResults对象
            FilterResults results = new FilterResults();

            /**
             * 没有搜索内容的话就还是给results赋值原始数据的值和大小
             * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
             *
             */
            if(TextUtils.isEmpty(constraint)){
                results.values = original;
                results.count = original.size();
            }else {
                // 创建集合保存过滤后的数据
                List<String> mList = new ArrayList<String>();
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for(String s: original){
                    // 这里就是过滤规则的具体实现
                    if(s.trim().toLowerCase().contains(constraint.toString().trim().toLowerCase())){
                        // 规则匹配的话就往集合中添加该数据
                        mList.add(s);
                    }
                }
                results.values = mList;
                results.count = mList.size();
            }

            // 返回FilterResults对象
            return results;
        }

        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // 获取过滤后的数据
            list = (List<String>) results.values;
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            if(listener != null){
                listener.getFilterData(list);
            }
            // 刷新数据源显示
            notifyDataSetChanged();
        }

    }

    class ViewHolder {
        TextView cityName;
    }

}

