package com.outr.jefe.server.config

import java.net.URI

case class ProxyConfig(enabled: Boolean = false,
                       inbound: List[Inbound],
                       outbound: URI)
