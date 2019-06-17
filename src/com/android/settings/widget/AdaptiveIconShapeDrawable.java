package com.android.settings.widget;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.PathParser;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AdaptiveIconShapeDrawable extends ShapeDrawable {
    public AdaptiveIconShapeDrawable() {}

    public AdaptiveIconShapeDrawable(Resources resources) {
        init(resources);
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Theme theme) throws XmlPullParserException, IOException {
        super.inflate(resources, xmlPullParser, attributeSet, theme);
        init(resources);
    }

    public void init(Resources resources) {
        int iconPath = resources.getSystem().getIdentifier("config_icon_mask", "string", "android");
        setShape(new PathShape(new Path(PathParser.createPathFromPathData(resources.getString(iconPath))), 100.0f, 100.0f));
    }
}
