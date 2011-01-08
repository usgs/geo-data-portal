package gov.usgs.cida.gdp.wps.algorithm.descriptor;

/**
 *
 * @author tkunicki
 */
public abstract class Descriptor {

    private final String identifier;
    private final String title;
    private final String abstrakt; // want 'abstract' but it's a java keyword

	Descriptor(Builder<? extends Builder<?>> builder) {
        this.identifier = builder.identifier;
        this.title = builder.title;
        this.abstrakt = builder.abstrakt;
    }

    public boolean hasIdentifier() {
        return identifier != null && identifier.length() > 0;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasTitle() {
        return getTitle() != null && title.length() > 0;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasAbstract() {
        return getAbstract() != null && abstrakt.length() > 0;
    }

    public String getAbstract() {
        return abstrakt;
    }

    public static abstract class Builder<B extends Builder<B>> {

        private String identifier;
        private String title;
        private String abstrakt; // want 'abstract' but it's a java keyword

        public B identifier(String identifier) {
            this.identifier = identifier;
            return self();
        }

        public B title(String title) {
            this.title = title;
            return self();
        }

        // want 'abstract' but it's a java keyword
        public B abstrakt(String abstrakt) {
            this.abstrakt = abstrakt;
            return self();
        }

        protected abstract B self();
    }
    
}
