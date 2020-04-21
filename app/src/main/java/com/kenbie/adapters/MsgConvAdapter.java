package com.kenbie.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.kenbie.KenbieApplication;
import com.kenbie.R;
import com.kenbie.listeners.MsgUserActionListeners;
import com.kenbie.model.Message;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rajaw on 9/13/2017.
 */

public class MsgConvAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private ArrayList<Message> msgData;
    private LayoutInflater mLayoutInflater;
    private MsgUserActionListeners mActionListeners;
    private int userId;
    private String currentDate = "", timeDisplay = "", read, delivered;
    private DateFormat sdf, tdf, cdf;
    private RequestOptions options;
    private SharedPreferences mPref;

    public MsgConvAdapter(Context context, ArrayList<Message> mData, MsgUserActionListeners msgUserActionListeners, int userId) {
        mContext = context;
        mPref = mContext.getSharedPreferences("kPrefs", MODE_PRIVATE);
        read = mPref.getString("99", "Read");
        delivered = mPref.getString("100", "Delivered");
        msgData = mData;
        mLayoutInflater = LayoutInflater.from(mContext);
        mActionListeners = msgUserActionListeners;
        this.userId = userId;
        if (msgData == null)
            msgData = new ArrayList<>();
        currentDate = fetchCurrentData();
        options = new RequestOptions()
                .placeholder(R.drawable.img_place_holder)
                .error(R.drawable.img_place_holder)
                .priority(Priority.HIGH)
                .transforms(new CenterCrop(), new RoundedCorners(16));
    }


    public void refreshData(ArrayList<Message> mData) {
        msgData = mData;
        if (msgData == null)
            msgData = new ArrayList<>();
        notifyDataSetInvalidated();
    }

    public void immediateRefreshData(ArrayList<Message> mData) {
        msgData = mData;
        if (msgData == null)
            msgData = new ArrayList<>();
        notifyDataSetChanged();
    }

    public int getCount() {
        return msgData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.msg_conv_cell_view, parent, false);
            holder.leftItem = (LinearLayout) convertView.findViewById(R.id.left_item);
            holder.rightItem = (LinearLayout) convertView.findViewById(R.id.right_item);

            holder.lFile = (LinearLayout) convertView.findViewById(R.id.l_file);
            holder.rFile = (RelativeLayout) convertView.findViewById(R.id.r_file);
            holder.rImgProgress = (ProgressBar) convertView.findViewById(R.id.r_img_progress);

            holder.lFileImg = (ImageView) convertView.findViewById(R.id.l_file_icon);
            holder.rFileImg = (ImageView) convertView.findViewById(R.id.r_file_icon);
            holder.rBgImg = (ImageView) convertView.findViewById(R.id.r_bg_img);
            holder.lMsg = (TextView) convertView.findViewById(R.id.l_msg);
            holder.rMsg = (TextView) convertView.findViewById(R.id.r_msg);
            holder.rMsgStatus = (TextView) convertView.findViewById(R.id.r_msg_status);
            holder.lMsgTime = (TextView) convertView.findViewById(R.id.l_msg_time);
            holder.rMsgTime = (TextView) convertView.findViewById(R.id.r_msg_time);
            holder.lMsg.setTypeface(KenbieApplication.S_NORMAL);
            holder.rMsg.setTypeface(KenbieApplication.S_NORMAL);
            holder.rMsgStatus.setTypeface(KenbieApplication.S_NORMAL);
            holder.lMsgTime.setTypeface(KenbieApplication.S_NORMAL);
            holder.rMsgTime.setTypeface(KenbieApplication.S_NORMAL);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.leftItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListeners.userConStart(null);
            }
        });

        holder.rightItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListeners.userConStart(null);
            }
        });

        holder.lFileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgData.get(position).getAttachment_type() != null && !msgData.get(position).isLoaderEnable())
                    mActionListeners.updateFavStatus(1, position);
            }
        });

        holder.rFileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgData.get(position).getAttachment_type() != null && !msgData.get(position).isLoaderEnable())
                    mActionListeners.updateFavStatus(1, position);
            }
        });

        holder.rBgImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgData.get(position).getAttachment_type() != null && !msgData.get(position).isLoaderEnable())
                    mActionListeners.updateFavStatus(1, position);
            }
        });


        String msg = msgData.get(position).getMsg().replace("\n", "<br/>");
        if (msgData.get(position).getMsg_user_id() == userId) {
            holder.leftItem.setVisibility(View.GONE);
            holder.rightItem.setVisibility(View.VISIBLE);
            holder.rMsg.setText(Html.fromHtml(msg), TextView.BufferType.SPANNABLE);
            holder.rMsgTime.setText(convertTimeWithTimeZone(msgData.get(position).getMsg_time()));
            if (msgData.get(position).getAttachment() != null) {
                holder.rMsg.setVisibility(View.GONE);
                holder.rFile.setVisibility(View.VISIBLE);
                holder.rFileImg.setVisibility(View.VISIBLE);
                if (msgData.get(position).getAttachment_type() != null && msgData.get(position).getAttachment_type().equalsIgnoreCase("img")) {
                    if (msgData.get(position).getMsgId() == -1) {
                        holder.rFileImg.setVisibility(View.GONE);
                        holder.rBgImg.setVisibility(View.VISIBLE);
//                        holder.rBgImg.setImageBitmap(getImageBitmap(msgData.get(position)));
//                        Uri photoUri = Uri.fromFile( new File(msgData.get(position).getAttachmentPath()));

                        Glide.with(mContext).load(msgData.get(position).getImgUri()).apply(options).into(holder.rBgImg);
//                        Glide.with(mContext).load(new File(msgData.get(position).getAttachmentPath())) .into(holder.rFileImg);
                    } else if (msgData.get(position).getMediaType() != 0) {
                        holder.rFileImg.setVisibility(View.GONE);
                        holder.rBgImg.setVisibility(View.VISIBLE);
                        Glide.with(mContext).load(msgData.get(position).getImgUri()).apply(options).into(holder.rBgImg);
//                        Glide.with(mContext).load(msgData.get(position).getAttachment()).apply(options).into(holder.rFileImg);
                    } else {
                        holder.rFileImg.setVisibility(View.VISIBLE);
                        Glide.with(mContext).load(msgData.get(position).getAttachment()).apply(options).into(holder.rFileImg);
                    }

                } else {
                    holder.rFileImg.setBackgroundResource(R.drawable.ic_file);
                }
            } else {
                holder.rMsg.setVisibility(View.VISIBLE);
                holder.rFile.setVisibility(View.GONE);
                holder.rFileImg.setVisibility(View.GONE);
            }

            if (msgData.get(position).getMsg_status() == 0) {
                holder.rMsgStatus.setText(delivered);
                holder.rMsgStatus.setTextColor(mContext.getResources().getColor(R.color.c_b3b3b3));
                holder.rMsgStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_msg_deliverd_tick, 0);
            } else {
                holder.rMsgStatus.setText(read);
                holder.rMsgStatus.setTextColor(mContext.getResources().getColor(R.color.c_FFDFE0));
                holder.rMsgStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_msg_read_tick, 0);
            }
        } else {
            holder.leftItem.setVisibility(View.VISIBLE);
            holder.rightItem.setVisibility(View.GONE);
            holder.lMsg.setText(Html.fromHtml(msg), TextView.BufferType.SPANNABLE);
            holder.lMsgTime.setText(convertTimeWithTimeZone(msgData.get(position).getMsg_time()));
            if (msgData.get(position).getAttachment() != null) {
                holder.lMsg.setVisibility(View.GONE);
                holder.lFile.setVisibility(View.VISIBLE);
                holder.lFileImg.setVisibility(View.VISIBLE);
                if (msgData.get(position).getAttachment_type() != null && msgData.get(position).getAttachment_type().equalsIgnoreCase("img"))
                    Glide.with(mContext).load(msgData.get(position).getAttachment()).apply(options).into(holder.lFileImg);
                else
                    holder.lFileImg.setBackgroundResource(R.drawable.ic_file);
            } else {
                holder.lMsg.setVisibility(View.VISIBLE);
                holder.lFile.setVisibility(View.GONE);
                holder.lFileImg.setVisibility(View.GONE);
            }
        }

        if(msgData.get(position).isLoaderEnable())
            holder.rImgProgress.setVisibility(View.VISIBLE);
        else
            holder.rImgProgress.setVisibility(View.GONE);

        return convertView;
    }

    class ViewHolder {
        private LinearLayout leftItem, rightItem, lFile;
        private RelativeLayout rFile;
        private ProgressBar rImgProgress;
        private ImageView lFileImg, rFileImg, rBgImg;
        private TextView lMsg, rMsg, rMsgStatus, lMsgTime, rMsgTime;
    }

    private Bitmap getImageBitmap(Message message) {
        Bitmap bitmap = null;
        try {
            if (message.getMediaType() == 1) {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), message.getImgUri());
                if(bitmap != null) {
//                    bitmap = rotateImageIfRequired(bitmap, message.getImgUri());
//                    bitmap = getResizedBitmap(bitmap, 500);
                }
            }else
                bitmap = getBitmapFromUri(message.getImgUri());

            if (bitmap == null)
                bitmap = getBitmapFromUri(message.getImgUri());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private String fetchCurrentData() {
        String currentDate = "";
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
//            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //15 Sep, 2017, 02:12
            sdf = new SimpleDateFormat("dd MMM, yyyy, HH:mm"); //15 Sep, 2017, 02:12
            sdf.setTimeZone(tz);

            Date dateCurrent = new Date();
            cdf = new SimpleDateFormat("dd MMM, yyyy");
            cdf.setTimeZone(tz);
            currentDate = cdf.format(dateCurrent);

            tdf = new SimpleDateFormat("HH:mm");
            tdf.setTimeZone(tz);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentDate;
    }


    public String convertTimeWithTimeZone(String time) {
        String dateDisplay = "";

        try {
            Date date = sdf.parse(time);
//            if (currentDate != null && currentDate.equalsIgnoreCase(cdf.format(date)))
//                dateDisplay = tdf.format(date);
//            else
//                dateDisplay = cdf.format(date);
            dateDisplay = tdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return dateDisplay;
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.count = getCount();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    mContext.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 4;
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
//            image = rotateImageIfRequired(image, uri);
//            image = getResizedBitmap(image, 500);
            System.gc();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = mContext.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

//        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
