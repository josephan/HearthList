package com.mattpflance.hearthlist;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

/**
 * Creates a list of cards from a cursor to a RecyclerView
 */
public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardsAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private CardsAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    /**
     * Cache of the children views for a Card list item.
     */
    public class CardsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mCardView;
        public final TextView mManaTextView;
        public final FrameLayout mAttackView;
        public final FrameLayout mHealthView;
        public final ImageView mAttackIcon;
        public final ImageView mHealthIcon;
        public final TextView mAttackTextView;
        public final TextView mHealthTextView;
        public final TextView mCardNameView;
        public final TextView mCardDescView;

        public CardsAdapterViewHolder(View view) {
            super(view);
            mCardView = (ImageView) view.findViewById(R.id.card_image);
            mManaTextView = (TextView) view.findViewById(R.id.mana_textview);
            mAttackView = (FrameLayout) view.findViewById(R.id.attack_view);
            mHealthView = (FrameLayout) view.findViewById(R.id.health_view);
            mAttackIcon = (ImageView) view.findViewById(R.id.attack_icon);
            mHealthIcon = (ImageView) view.findViewById(R.id.health_icon);
            mAttackTextView = (TextView) view.findViewById(R.id.attack_textview);
            mHealthTextView = (TextView) view.findViewById(R.id.health_textview);
            mCardNameView = (TextView) view.findViewById(R.id.card_name_view);
            mCardDescView = (TextView) view.findViewById(R.id.card_text_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            // Handle onClick with given cursor
            mClickHandler.onClick(mCursor);
        }
    }

    public static interface CardsAdapterOnClickHandler {
        void onClick(Cursor cursor);
    }

    public CardsAdapter(Context context, CardsAdapterOnClickHandler dh, View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
    }

    @Override
    public CardsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_list_item_view, viewGroup, false);
            view.setFocusable(true);
            return new CardsAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(CardsAdapterViewHolder cardsAdapterVh, int position) {
        mCursor.moveToPosition(position);

        // Load card image
        Glide.with(mContext)
                .load(mCursor.getBlob(CardsFragment.COL_CARD_IMG))
                .crossFade()
                .into(cardsAdapterVh.mCardView);

        // Load mana cost
        cardsAdapterVh.mManaTextView.setText(mCursor.getInt(CardsFragment.COL_CARD_COST));

        // Determine which icons to show for attack and health
        String type = mCursor.getString(CardsFragment.COL_CARD_TYPE).toLowerCase();
        int attackId = -1;
        int healthId = -1;

        if (type.equals("minion")) {
            attackId = R.drawable.ic_minion_attack;
            healthId = R.drawable.ic_minion_health;
        } else if (type.equals("weapon")) {
            attackId = R.drawable.ic_weapon_attack;
            healthId = R.drawable.ic_weapon_health;
        }

        if (attackId == -1 && healthId == -1) {
            // This is a spell, hide the icons
            cardsAdapterVh.mAttackView.setVisibility(View.GONE);
            cardsAdapterVh.mHealthView.setVisibility(View.GONE);
        } else {
            // Otherwise, load icons
            Glide.with(mContext)
                    .load(attackId)
                    .crossFade()
                    .into(cardsAdapterVh.mAttackIcon);

            Glide.with(mContext)
                    .load(healthId)
                    .crossFade()
                    .into(cardsAdapterVh.mHealthIcon);

            // Now load the attack and health values
            cardsAdapterVh.mAttackTextView.setText(mCursor.getInt(CardsFragment.COL_CARD_ATTACK));
            cardsAdapterVh.mHealthTextView.setText(mCursor.getInt(CardsFragment.COL_CARD_HEALTH));
        }

        // Set card name
        cardsAdapterVh.mCardNameView.setText(mCursor.getString(CardsFragment.COL_CARD_NAME));

        // Set card text
        cardsAdapterVh.mCardDescView.setText(mCursor.getString(CardsFragment.COL_CARD_TEXT));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof CardsAdapterViewHolder ) {
            CardsAdapterViewHolder vfh = (CardsAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}