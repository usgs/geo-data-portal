package gov.usgs.cida.gdp.wps.algorithm;

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

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasTitle() {
        return getTitle() != null;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasAbstract() {
        return getAbstract() != null;
    }

    public String getAbstract() {
        return abstrakt;
    }

    static abstract class Builder<B extends Builder<B>> {

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

        public abstract Descriptor build();
    }
    
}
