package com.edasaki.misakachan.test.tests;

import com.edasaki.misakachan.test.annotations.TestClass;
import com.edasaki.misakachan.test.annotations.TestMethod;
import com.edasaki.misakachan.utils.logging.MTimer;
import com.edasaki.misakachan.web.FinishedCondition;
import com.edasaki.misakachan.web.WebAccessor;

@TestClass(solo = false)
public class WebAccessorTests {

    @TestMethod
    public void testGet() {
        WebAccessor.initialize();
        MTimer timer = new MTimer();
        WebAccessor.getURL("http://www.mangahere.co/manga/gekkan_shoujo_nozaki_kun/");
        WebAccessor.getURL("http://kissmanga.com/manga/yotsubato", new FinishedCondition() {
            @Override
            public boolean finished(String src) {
                return src.contains("class=\"listing\"");
            }
        });
        //        M.debug(doc.select(".listing"));
        timer.output("WebAccessor done in");
        timer.reset();
        WebAccessor.getURL("http://kissmanga.com/Manga/Gekkan-Shoujo-Nozaki-kun-Anthology");
        //        M.debug(doc.select(".listing"));
        timer.output("WebAccessor done in");
        timer.reset();
        WebAccessor.getURL("http://kissmanga.com/Manga/Shinryaku-Ika-Musume");
        //        M.debug(doc.select(".listing"));
        timer.output("WebAccessor done in");
    }
}