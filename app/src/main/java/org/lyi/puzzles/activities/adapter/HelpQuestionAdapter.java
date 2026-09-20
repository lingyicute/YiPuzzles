/*
 This file is part of YiPuzzles. This app implements the functions of the
 game 2048 in a privacy friendly version.

 YiPuzzles is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 YiPuzzles is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YiPuzzles. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lyi.puzzles.activities.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.lyi.puzzles.R;

import java.util.List;

/**
 * Material 3 FAQ list for the help screen.
 * <p>
 * Each question is a filled {@code MaterialCardView} whose answer section
 * expands/collapses in place. The interaction follows the Material 3 spec:
 * <ul>
 *   <li><b>State layer</b> - the card is clickable/focusable, so the Material
 *       components library draws the ripple state layer for press/focus/hover.</li>
 *   <li><b>Motion</b> - the answer height and the chevron rotation are animated
 *       with the theme's {@code motionDuration*} and
 *       {@code motionEasingEmphasized*Interpolator} tokens (emphasized decelerate
 *       when the content enters, emphasized accelerate when it exits).</li>
 *   <li><b>Accessibility</b> - the expanded/collapsed state is announced through
 *       {@link ViewCompat#setStateDescription(View, CharSequence)}.</li>
 * </ul>
 * <p>
 * Replaces the former {@code ExpandableListView} based adapter: that legacy
 * framework widget is not a Material component, expands instantly without
 * motion and offers no state layer.
 */
public class HelpQuestionAdapter extends RecyclerView.Adapter<HelpQuestionAdapter.QuestionViewHolder> {

    /** One FAQ entry: question, answer and the current expansion state. */
    public static class HelpItem {

        private final String question;
        private final String answer;
        private boolean expanded;

        public HelpItem(@NonNull String question, @NonNull String answer, boolean expanded) {
            this.question = question;
            this.answer = answer;
            this.expanded = expanded;
        }

        @NonNull
        public String getQuestion() {
            return question;
        }

        @NonNull
        public String getAnswer() {
            return answer;
        }

        public boolean isExpanded() {
            return expanded;
        }
    }

    /*
     * Fallbacks for the Material 3 motion tokens, taken from the spec
     * (m3.material.io/styles/motion): medium2 = 300ms, medium1 = 250ms and the
     * control points of the emphasized decelerate / accelerate curves. They are
     * only used if the theme does not provide the attributes.
     */
    private static final int FALLBACK_EXPAND_DURATION_MS = 300;
    private static final int FALLBACK_COLLAPSE_DURATION_MS = 250;
    private static final Interpolator FALLBACK_EXPAND_INTERPOLATOR =
            new PathInterpolator(0.05f, 0.7f, 0.1f, 1f);
    private static final Interpolator FALLBACK_COLLAPSE_INTERPOLATOR =
            new PathInterpolator(0.3f, 0f, 0.8f, 0.15f);

    private final List<HelpItem> items;
    private final int expandDuration;
    private final int collapseDuration;
    private final Interpolator expandInterpolator;
    private final Interpolator collapseInterpolator;
    private final String stateExpanded;
    private final String stateCollapsed;

    public HelpQuestionAdapter(@NonNull Context context, @NonNull List<HelpItem> items) {
        this.items = items;
        this.expandDuration = resolveThemeDuration(context,
                com.google.android.material.R.attr.motionDurationMedium2,
                FALLBACK_EXPAND_DURATION_MS);
        this.collapseDuration = resolveThemeDuration(context,
                com.google.android.material.R.attr.motionDurationMedium1,
                FALLBACK_COLLAPSE_DURATION_MS);
        this.expandInterpolator = resolveThemeInterpolator(context,
                com.google.android.material.R.attr.motionEasingEmphasizedDecelerateInterpolator,
                FALLBACK_EXPAND_INTERPOLATOR);
        this.collapseInterpolator = resolveThemeInterpolator(context,
                com.google.android.material.R.attr.motionEasingEmphasizedAccelerateInterpolator,
                FALLBACK_COLLAPSE_INTERPOLATOR);
        this.stateExpanded = context.getString(R.string.help_state_expanded);
        this.stateCollapsed = context.getString(R.string.help_state_collapsed);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_help_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        final HelpItem item = items.get(position);

        holder.questionView.setText(item.getQuestion());
        holder.answerView.setText(item.getAnswer());

        // a recycled view may still be running an expand/collapse animation
        holder.cancelAnimations();
        applyExpandedState(holder, item.isExpanded(), false);

        ViewCompat.setStateDescription(holder.itemView,
                item.isExpanded() ? stateExpanded : stateCollapsed);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(holder, item);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull QuestionViewHolder holder) {
        holder.cancelAnimations();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** The expansion state of all items; used to survive configuration changes. */
    @NonNull
    public boolean[] getExpandedState() {
        boolean[] state = new boolean[items.size()];
        for (int i = 0; i < items.size(); i++) {
            state[i] = items.get(i).isExpanded();
        }
        return state;
    }

    private void toggle(@NonNull QuestionViewHolder holder, @NonNull HelpItem item) {
        item.expanded = !item.isExpanded();
        ViewCompat.setStateDescription(holder.itemView,
                item.isExpanded() ? stateExpanded : stateCollapsed);
        applyExpandedState(holder, item.isExpanded(), true);
    }

    /**
     * Sets (or, when {@code animate} is true, transitions to) the expanded
     * state of one card: the answer section grows/shrinks with the Material 3
     * emphasized easing while the chevron rotates and the content fades.
     */
    private void applyExpandedState(@NonNull final QuestionViewHolder holder,
                                    final boolean expanded, boolean animate) {
        holder.cancelAnimations();
        final View container = holder.answerContainer;
        final ImageView indicator = holder.indicatorView;

        if (!animate) {
            indicator.setRotation(expanded ? 180f : 0f);
            container.setVisibility(expanded ? View.VISIBLE : View.GONE);
            container.setAlpha(1f);
            setContainerHeight(container, ViewGroup.LayoutParams.WRAP_CONTENT);
            return;
        }

        final float targetRotation = expanded ? 180f : 0f;
        // Interpolate from whatever state the container is currently in, so a
        // toggle in the middle of a running animation stays seamless (the start
        // values of a fresh expand are 0, because the container is GONE and has
        // never been laid out or still has height 0).
        final int startHeight = container.getHeight();
        final float startAlpha = startHeight > 0 ? container.getAlpha() : 0f;
        final int targetHeight;
        if (expanded) {
            container.setVisibility(View.VISIBLE);
            container.measure(widthMeasureSpec(container),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            targetHeight = container.getMeasuredHeight();
        } else {
            targetHeight = 0;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(expanded ? expandDuration : collapseDuration);
        animator.setInterpolator(expanded ? expandInterpolator : collapseInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                int height = Math.round(startHeight + (targetHeight - startHeight) * fraction);
                setContainerHeight(container, height);
                float alpha = expanded
                        ? startAlpha + (1f - startAlpha) * fraction
                        : startAlpha * (1f - fraction);
                container.setAlpha(alpha);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                holder.animator = null;
                if (cancelled) {
                    return; // a new animation (or a rebind) takes over immediately
                }
                container.setVisibility(expanded ? View.VISIBLE : View.GONE);
                container.setAlpha(1f);
                setContainerHeight(container, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        holder.animator = animator;
        animator.start();

        indicator.animate()
                .rotation(targetRotation)
                .setDuration(expanded ? expandDuration : collapseDuration)
                .setInterpolator(expanded ? expandInterpolator : collapseInterpolator)
                .start();
    }

    private static void setContainerHeight(@NonNull View container, int height) {
        ViewGroup.LayoutParams params = container.getLayoutParams();
        params.height = height;
        container.setLayoutParams(params);
    }

    /**
     * Measure spec for the answer container: exactly as wide as the card's
     * content area, so the measured height matches the later layout pass.
     */
    private static int widthMeasureSpec(@NonNull View container) {
        ViewGroup parent = (ViewGroup) container.getParent();
        int width = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        return View.MeasureSpec.makeMeasureSpec(Math.max(width, 0), View.MeasureSpec.EXACTLY);
    }

    private static int resolveThemeDuration(@NonNull Context context, int attrResId, int fallback) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, value, true)
                && value.type >= TypedValue.TYPE_FIRST_INT
                && value.type <= TypedValue.TYPE_LAST_INT) {
            return value.data;
        }
        return fallback;
    }

    private static Interpolator resolveThemeInterpolator(@NonNull Context context, int attrResId,
                                                         @NonNull Interpolator fallback) {
        TypedValue value = new TypedValue();
        try {
            if (context.getTheme().resolveAttribute(attrResId, value, true) && value.resourceId != 0) {
                return AnimationUtils.loadInterpolator(context, value.resourceId);
            }
        } catch (Exception ignored) {
            // unknown easing resource: use the spec-defined fallback curve
        }
        return fallback;
    }

    /** One FAQ card. */
    static class QuestionViewHolder extends RecyclerView.ViewHolder {

        final TextView questionView;
        final TextView answerView;
        final View answerContainer;
        final ImageView indicatorView;
        ValueAnimator animator;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionView = itemView.findViewById(R.id.helpQuestion);
            answerView = itemView.findViewById(R.id.helpAnswer);
            answerContainer = itemView.findViewById(R.id.helpAnswerContainer);
            indicatorView = itemView.findViewById(R.id.expandIndicator);
        }

        void cancelAnimations() {
            if (animator != null) {
                animator.removeAllListeners();
                animator.removeAllUpdateListeners();
                animator.cancel();
                animator = null;
            }
            indicatorView.animate().cancel();
        }
    }
}
