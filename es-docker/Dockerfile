#
# Custom Elasticsearch Image based 
# https://github.com/docker-library/elasticsearch
#
FROM elasticsearch:2.3.2
MAINTAINER Adam Mollenkopf <adam.mollenkopf@gmail.com>

COPY config /usr/share/elasticsearch/config

EXPOSE 9200 9300
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["elasticsearch"]
