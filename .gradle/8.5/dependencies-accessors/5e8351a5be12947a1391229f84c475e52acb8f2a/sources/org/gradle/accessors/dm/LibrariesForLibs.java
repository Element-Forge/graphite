package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final GraphqlLibraryAccessors laccForGraphqlLibraryAccessors = new GraphqlLibraryAccessors(owner);
    private final JacksonLibraryAccessors laccForJacksonLibraryAccessors = new JacksonLibraryAccessors(owner);
    private final JetbrainsLibraryAccessors laccForJetbrainsLibraryAccessors = new JetbrainsLibraryAccessors(owner);
    private final MicrometerLibraryAccessors laccForMicrometerLibraryAccessors = new MicrometerLibraryAccessors(owner);
    private final Slf4jLibraryAccessors laccForSlf4jLibraryAccessors = new Slf4jLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

        /**
         * Creates a dependency provider for javapoet (com.squareup:javapoet)
     * with versionRef 'javapoet'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJavapoet() {
            return create("javapoet");
    }

    /**
     * Returns the group of libraries at graphql
     */
    public GraphqlLibraryAccessors getGraphql() {
        return laccForGraphqlLibraryAccessors;
    }

    /**
     * Returns the group of libraries at jackson
     */
    public JacksonLibraryAccessors getJackson() {
        return laccForJacksonLibraryAccessors;
    }

    /**
     * Returns the group of libraries at jetbrains
     */
    public JetbrainsLibraryAccessors getJetbrains() {
        return laccForJetbrainsLibraryAccessors;
    }

    /**
     * Returns the group of libraries at micrometer
     */
    public MicrometerLibraryAccessors getMicrometer() {
        return laccForMicrometerLibraryAccessors;
    }

    /**
     * Returns the group of libraries at slf4j
     */
    public Slf4jLibraryAccessors getSlf4j() {
        return laccForSlf4jLibraryAccessors;
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class GraphqlLibraryAccessors extends SubDependencyFactory {

        public GraphqlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for java (com.graphql-java:graphql-java)
         * with versionRef 'graphql.java'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJava() {
                return create("graphql.java");
        }

    }

    public static class JacksonLibraryAccessors extends SubDependencyFactory {
        private final JacksonDatatypeLibraryAccessors laccForJacksonDatatypeLibraryAccessors = new JacksonDatatypeLibraryAccessors(owner);

        public JacksonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for databind (com.fasterxml.jackson.core:jackson-databind)
         * with versionRef 'jackson'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getDatabind() {
                return create("jackson.databind");
        }

        /**
         * Returns the group of libraries at jackson.datatype
         */
        public JacksonDatatypeLibraryAccessors getDatatype() {
            return laccForJacksonDatatypeLibraryAccessors;
        }

    }

    public static class JacksonDatatypeLibraryAccessors extends SubDependencyFactory {

        public JacksonDatatypeLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jsr310 (com.fasterxml.jackson.datatype:jackson-datatype-jsr310)
         * with versionRef 'jackson'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJsr310() {
                return create("jackson.datatype.jsr310");
        }

    }

    public static class JetbrainsLibraryAccessors extends SubDependencyFactory {

        public JetbrainsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for annotations (org.jetbrains:annotations)
         * with versionRef 'jetbrains.annotations'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getAnnotations() {
                return create("jetbrains.annotations");
        }

    }

    public static class MicrometerLibraryAccessors extends SubDependencyFactory {

        public MicrometerLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (io.micrometer:micrometer-core)
         * with versionRef 'micrometer'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() {
                return create("micrometer.core");
        }

    }

    public static class Slf4jLibraryAccessors extends SubDependencyFactory {

        public Slf4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (org.slf4j:slf4j-api)
         * with versionRef 'slf4j'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("slf4j.api");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final GraphqlVersionAccessors vaccForGraphqlVersionAccessors = new GraphqlVersionAccessors(providers, config);
        private final JetbrainsVersionAccessors vaccForJetbrainsVersionAccessors = new JetbrainsVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jackson (2.17.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJackson() { return getVersion("jackson"); }

            /**
             * Returns the version associated to this alias: javapoet (1.13.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJavapoet() { return getVersion("javapoet"); }

            /**
             * Returns the version associated to this alias: micrometer (1.12.4)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getMicrometer() { return getVersion("micrometer"); }

            /**
             * Returns the version associated to this alias: slf4j (2.0.12)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSlf4j() { return getVersion("slf4j"); }

        /**
         * Returns the group of versions at versions.graphql
         */
        public GraphqlVersionAccessors getGraphql() {
            return vaccForGraphqlVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.jetbrains
         */
        public JetbrainsVersionAccessors getJetbrains() {
            return vaccForJetbrainsVersionAccessors;
        }

    }

    public static class GraphqlVersionAccessors extends VersionFactory  {

        public GraphqlVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: graphql.java (22.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJava() { return getVersion("graphql.java"); }

    }

    public static class JetbrainsVersionAccessors extends VersionFactory  {

        public JetbrainsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jetbrains.annotations (24.1.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAnnotations() { return getVersion("jetbrains.annotations"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

    }

}
