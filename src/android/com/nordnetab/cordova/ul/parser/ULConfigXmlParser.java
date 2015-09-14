package com.nordnetab.cordova.ul.parser;

import android.content.Context;

import com.nordnetab.cordova.ul.model.ULHost;
import com.nordnetab.cordova.ul.model.ULPath;

import org.apache.cordova.ConfigXmlParser;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 09.09.15.
 */
public class ULConfigXmlParser  extends ConfigXmlParser {

    private final Context context;
    private List<ULHost> hostsList;

    private boolean isInsideMainTag;
    private boolean didParseMainBlock;
    private boolean isInsideHostBlock;
    private ULHost processedHost;

    public ULConfigXmlParser(Context context) {
        this.context = context;
    }

    public List<ULHost> parse() {
        resetValuesToDefaultState();
        super.parse(context);

        return hostsList;
    }

    private void resetValuesToDefaultState() {
        hostsList = new ArrayList<ULHost>();
        isInsideMainTag = false;
        didParseMainBlock = false;
        isInsideHostBlock = false;
        processedHost = null;
    }

    @Override
    public void handleStartTag(XmlPullParser xml) {
        if (didParseMainBlock) {
            return;
        }

        final String name = xml.getName();
        if (!isInsideMainTag && XmlTags.MAIN_TAG.equals(name)) {
            isInsideMainTag = true;
            return;
        }

        if (!isInsideMainTag) {
            return;
        }

        if (!isInsideHostBlock && XmlTags.HOST_TAG.equals(name)) {
            isInsideHostBlock = true;
            processHostBlock(xml);
            return;
        }

        if (isInsideHostBlock && XmlTags.PATH_TAG.equals(name)) {
            processPathBlock(xml);
        }
    }

    @Override
    public void handleEndTag(XmlPullParser xml) {
        if (didParseMainBlock) {
            return;
        }

        final String name = xml.getName();

        if (isInsideHostBlock && XmlTags.HOST_TAG.equals(name)) {
            isInsideHostBlock = false;
            hostsList.add(processedHost);
            processedHost = null;
            return;
        }

        if (XmlTags.MAIN_TAG.equals(name)) {
            isInsideMainTag = false;
            didParseMainBlock = true;
        }
    }

    private void processHostBlock(XmlPullParser xml) {
        final String hostName = xml.getAttributeValue(null, XmlTags.HOST_NAME_ATTRIBUTE);
        final String eventName = xml.getAttributeValue(null, XmlTags.HOST_EVENT_ATTRIBUTE);
        final String scheme = xml.getAttributeValue(null, XmlTags.HOST_SCHEME_ATTRIBUTE);

        processedHost = new ULHost(hostName, scheme, eventName);
    }

    private void processPathBlock(XmlPullParser xml) {
        final String url = xml.getAttributeValue(null, XmlTags.PATH_URL_TAG);
        // skip wildcard urls
        if ("*".equals(url) || ".*".equals(url)) {
            return;
        }

        String event = xml.getAttributeValue(null, XmlTags.PATH_EVENT_TAG);
        if (event == null) {
            event = processedHost.getEvent();
        }

        ULPath path = new ULPath(url, event);
        processedHost.getPaths().add(path);
    }
}