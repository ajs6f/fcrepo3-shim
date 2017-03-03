package edu.si.fcrepo3;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

import org.apache.commons.lang3.ArrayUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;

public class TestResourceOption extends DefaultCompositeOption {
    
    private static final Option[] supportBundles = new Option[] {
            mavenBundle("org.ops4j.pax.tinybundles", "tinybundles", "2.1.1"),
            mavenBundle("biz.aQute.bnd", "bndlib", "2.4.0"), };

    @Override
    public Option[] getOptions() {
        return ArrayUtils.addAll(supportBundles, super.getOptions());
    }

    public static TestResourceOption testResources() {
        return new TestResourceOption();
    }

}
