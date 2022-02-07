const version = "v1.0";
const staticCacheName = "static-cache-" + version;
const cacheFiles = [];

self.addEventListener("install", (event) => {
  const preCache = async () => {
    const cache = await caches.open(staticCacheName);
    return cache.addAll(cacheFiles);
  };
  event.waitUntil(preCache());
});

self.addEventListener("activate", function (event) {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys
          .filter((key) => key !== staticCacheName)
          .map((key) => caches.delete(key))
      );
    })
  );
});

self.addEventListener("message", function (messageEvent) {
  if (messageEvent.data.action === "skipWaiting") return skipWaiting();
});

self.addEventListener("fetch", (event) => {
  event.respondWith(
    caches.match(event.request).then((cachesRes) => {
      return cachesRes || fetch(event.request);
    })
  );
});
