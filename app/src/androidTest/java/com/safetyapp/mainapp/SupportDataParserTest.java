package com.safetyapp.mainapp;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.*;


@RunWith(AndroidJUnit4.class)
public class SupportDataParserTest {

    private SupportDataParser parser;

    @Before
    public void setUp() {

        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        parser = new SupportDataParser(appContext);
    }

    @Test
    public void parse_shouldReturnNonEmptyMap() {

        HashMap<String, List<SupportResource>> supportData = parser.parse();


        assertNotNull(supportData);
        assertFalse(supportData.isEmpty());
    }

    @Test
    public void parse_shouldContainDataForToronto() {
        HashMap<String, List<SupportResource>> supportData = parser.parse();

        assertTrue(supportData.containsKey("Toronto"));
        List<SupportResource> torontoServices = supportData.get("Toronto");

        assertNotNull(torontoServices);
        assertFalse(torontoServices.isEmpty());
    }

    @Test
    public void parse_firstTorontoServiceShouldHaveCorrectName() {
        HashMap<String, List<SupportResource>> supportData = parser.parse();
        List<SupportResource> torontoServices = supportData.get("Toronto");


        String expectedFirstName = "Toronto Police Service";
        String actualFirstName = torontoServices.get(0).getName();

        assertEquals(expectedFirstName, actualFirstName);
    }
}