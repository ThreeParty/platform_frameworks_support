/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.core.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.support.v4.BaseInstrumentationTestCase;
import android.view.Display;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.test.R;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewCompatTest extends BaseInstrumentationTestCase<ViewCompatActivity> {

    private View mView;

    public ViewCompatTest() {
        super(ViewCompatActivity.class);
    }

    @Before
    public void setUp() {
        final Activity activity = mActivityTestRule.getActivity();
        mView = activity.findViewById(R.id.view);
    }

    @Test
    public void testConstants() {
        // Compat constants must match core constants since they can be used interchangeably
        // in various support lib calls.
        assertEquals("LTR constants", View.LAYOUT_DIRECTION_LTR, ViewCompat.LAYOUT_DIRECTION_LTR);
        assertEquals("RTL constants", View.LAYOUT_DIRECTION_RTL, ViewCompat.LAYOUT_DIRECTION_RTL);
    }

    @Test
    public void testGetDisplay() {
        final Display display = ViewCompat.getDisplay(mView);
        assertNotNull(display);
    }

    @Test
    public void testGetDisplay_returnsNullForUnattachedView() {
        final View view = new View(mActivityTestRule.getActivity());
        final Display display = ViewCompat.getDisplay(view);
        assertNull(display);
    }

    @Test
    public void testTransitionName() {
        final View view = new View(mActivityTestRule.getActivity());
        ViewCompat.setTransitionName(view, "abc");
        assertEquals("abc", ViewCompat.getTransitionName(view));
    }

    @Test
    public void  dispatchNestedScroll_viewIsNestedScrollingChild3_callsCorrectMethod() {
        final NestedScrollingChild3Impl nestedScrollingChild3Impl =
                mock(NestedScrollingChild3Impl.class);

        ViewCompat.dispatchNestedScroll(
                nestedScrollingChild3Impl,
                1,
                2,
                3,
                4,
                new int[]{5, 6},
                ViewCompat.TYPE_TOUCH,
                new int[]{9, 10});

        verify(nestedScrollingChild3Impl).dispatchNestedScroll(
                1,
                2,
                3,
                4,
                new int[]{5, 6},
                ViewCompat.TYPE_TOUCH,
                new int[]{9, 10});
        verify(nestedScrollingChild3Impl, never()).dispatchNestedScroll(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                any(int[].class),
                anyInt());
        verify(nestedScrollingChild3Impl, never()).dispatchNestedScroll(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                any(int[].class));
    }

    @Test
    public void  dispatchNestedScroll_viewIsNestedScrollingChild2_callsCorrectMethod() {
        final NestedScrollingChild2Impl nestedScrollingChild2Impl =
                mock(NestedScrollingChild2Impl.class);

        ViewCompat.dispatchNestedScroll(
                nestedScrollingChild2Impl,
                1,
                2,
                3,
                4,
                new int[]{5, 6},
                ViewCompat.TYPE_TOUCH,
                new int[]{9, 10});

        verify(nestedScrollingChild2Impl).dispatchNestedScroll(
                1,
                2,
                3,
                4,
                new int[]{5, 6},
                ViewCompat.TYPE_TOUCH);
        verify(nestedScrollingChild2Impl, never()).dispatchNestedScroll(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                any(int[].class));
    }

    @Test
    public void  dispatchNestedScroll_viewIsNscTouchTypeNotTouch_callsNothing() {
        final NestedScrollingChildImpl nestedScrollingChildImpl =
                mock(NestedScrollingChildImpl.class);

        ViewCompat.dispatchNestedScroll(
                nestedScrollingChildImpl,
                11,
                2,
                3,
                4,
                new int[]{5, 6},
                ViewCompat.TYPE_NON_TOUCH,
                new int[]{9, 10});

        verify(nestedScrollingChildImpl, never()).dispatchNestedScroll(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                any(int[].class));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21)
    public void  dispatchNestedScroll_viewIsNotAndroidXNestedScrollingChild_callsCorrectMethod() {
        final ViewSubclass viewSubclass = mock(ViewSubclass.class);

        ViewCompat.dispatchNestedScroll(
                viewSubclass,
                1,
                2,
                3,
                4,
                new int[]{5, 6},
                ViewCompat.TYPE_TOUCH,
                new int[]{9, 10});

        verify(viewSubclass).dispatchNestedScroll(
                1,
                2,
                3,
                4,
                new int[]{5, 6});
    }

    @Test
    public void testGenerateViewId() {
        final int requestCount = 100;

        Set<Integer> generatedIds = new HashSet<>();
        for (int i = 0; i < requestCount; i++) {
            int generatedId = ViewCompat.generateViewId();
            assertTrue(isViewIdGenerated(generatedId));
            generatedIds.add(generatedId);
        }

        assertThat(generatedIds.size(), equalTo(requestCount));
    }

    @Test
    public void testRequireViewByIdFound() {
        View container = mActivityTestRule.getActivity().findViewById(R.id.container);
        assertSame(mView, ViewCompat.requireViewById(container, R.id.view));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequireViewByIdMissing() {
        View container = mActivityTestRule.getActivity().findViewById(R.id.container);

        // action_bar isn't present inside container
        ViewCompat.requireViewById(container, R.id.action_bar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequireViewByIdInvalid() {
        View container = mActivityTestRule.getActivity().findViewById(R.id.container);

        // NO_ID is always invalid
        ViewCompat.requireViewById(container, View.NO_ID);
    }

    private static boolean isViewIdGenerated(int id) {
        return (id & 0xFF000000) == 0 && (id & 0x00FFFFFF) != 0;
    }

    public abstract static class ViewSubclass extends View {
        public ViewSubclass(Context context) {
            super(context);
        }
    }

    public abstract static class NestedScrollingChildImpl extends ViewSubclass
            implements NestedScrollingChild{
        public NestedScrollingChildImpl(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                int dyUnconsumed, @Nullable int[] offsetInWindow) {
            return true;
        }
    }

    public abstract static class NestedScrollingChild2Impl extends NestedScrollingChildImpl
            implements NestedScrollingChild2 {

        public NestedScrollingChild2Impl(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
            return true;
        }
    }

    public abstract static class NestedScrollingChild3Impl extends NestedScrollingChild2Impl
            implements NestedScrollingChild3 {

        public NestedScrollingChild3Impl(Context context) {
            super(context);
        }

        @Override
        public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                int dyUnconsumed, @Nullable int[] offsetInWindow, int type,
                @Nullable int[] consumed) {
        }
    }
}
