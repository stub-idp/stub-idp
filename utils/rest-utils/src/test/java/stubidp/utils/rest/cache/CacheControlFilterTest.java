package stubidp.utils.rest.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CacheControlFilterTest {
    private CacheControlFilter filter;

    @BeforeEach
    void setUp() {
        AssetCacheConfiguration assetCacheConfiguration = new AssetCacheConfiguration() {
            @Override
            public boolean shouldCacheAssets() {
                return false;
            }

            @Override
            public String getAssetsCacheDuration() {
                return null;
            }
        };
        filter = new CacheControlFilter(assetCacheConfiguration) {
            @Override
            protected boolean isCacheableAsset(String localAddr) {
                return false;
            }
        };
    }

    @Test
    void doFilter_shouldSetHeadersToPreventCaching() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("randomaddress");
        filter.doFilter(mockRequest, response, mock(FilterChain.class));

        verify(response).setHeader("Cache-Control", "no-cache, no-store");
        verify(response).setHeader("Pragma", "no-cache");
    }

    @Test
    void doFilter_shouldCallFilterChainAfterSettingHeaders() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        doThrow(new IOException()).when(chain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        try {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            when(mockRequest.getRequestURI()).thenReturn("randomaddress");
            filter.doFilter(mockRequest, response, chain);
        } catch (IOException | ServletException e) {
        }

        verify(response, times(2)).setHeader(any(String.class), any(String.class));
    }
}
