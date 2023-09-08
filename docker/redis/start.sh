docker run -d --name redis --privileged=true \
-p 6379:6379 \
-v ~/Documents/Proj/Campfire/docker/redis/conf:/usr/local/etc/redis \
redis redis-server /usr/local/etc/redis/redis.conf
