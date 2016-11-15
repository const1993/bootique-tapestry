package io.bootique.tapestry.filter;

import com.google.inject.Injector;
import io.bootique.jetty.MappedFilter;
import io.bootique.tapestry.di.GuiceTapestryModule;
import io.bootique.tapestry.di.InjectorModuleDef;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.services.MapSymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BQTapestryFilterFactory {

    protected String urlPattern;
    protected int filterOrder;

    protected String name;

    // properties from http://tapestry.apache.org/configuration.html

    protected Boolean productionMode;
    protected String executionModes;
    protected String supportedLocales;
    protected String charset;
    protected String appPackage;

    public BQTapestryFilterFactory() {
        this.urlPattern = "/*";
        this.filterOrder = 1;
        this.name = "tapestry";

        this.productionMode = true;
        this.executionModes = "production";
        this.charset = "UTF-8";
    }

    public void setProductionMode(Boolean productionMode) {
        this.productionMode = productionMode;
    }

    public void setExecutionModes(String executionModes) {
        this.executionModes = executionModes;
    }

    public void setSupportedLocales(String supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public MappedFilter createTapestryFilter(Injector injector, Map<String, String> diSymbols) {
        SymbolProvider symbolProvider = createSymbolProvider(diSymbols);
        BQTapestryFilter filter = new BQTapestryFilter(name, symbolProvider, extraModules(), extraModuleDefs(injector));
        return new MappedFilter(filter, Collections.singleton(urlPattern), name, filterOrder);
    }

    protected Class[] extraModules() {
        return new Class[]{GuiceTapestryModule.class};
    }

    protected ModuleDef[] extraModuleDefs(Injector injector) {
        ModuleDef guiceBridge = new InjectorModuleDef(injector);

        return new ModuleDef[]{guiceBridge};
    }

    protected SymbolProvider createSymbolProvider(Map<String, String> diSymbols) {

        Map<String, String> params = new HashMap<>(diSymbols);

        // override DI symbols if set explicitly in the factory
        if (productionMode != null) {
            params.put(SymbolConstants.PRODUCTION_MODE, Boolean.toString(productionMode));
        }

        if (executionModes != null) {
            params.put(SymbolConstants.EXECUTION_MODE, executionModes);
        }

        if (charset != null) {
            params.put(SymbolConstants.CHARSET, charset);
        }

        if (appPackage != null) {
            params.put(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage);
        } else {
            // sanity check
            if (!params.containsKey(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)) {
                throw new IllegalStateException("Tapestry app package is not defined. Use 'tapestry.appPackage' config " +
                        "or inject 'tapestry.app-package' symbol with this value.");
            }
        }

        if (supportedLocales != null) {
            params.put(SymbolConstants.SUPPORTED_LOCALES, supportedLocales);
        }

        // provide default values for symbols if not defined in DI
        if (!params.containsKey(SymbolConstants.GZIP_COMPRESSION_ENABLED)) {
            // compression should be configured at the Jetty level
            params.put(SymbolConstants.GZIP_COMPRESSION_ENABLED, "false");
        }

        return new MapSymbolProvider(params);
    }
}
