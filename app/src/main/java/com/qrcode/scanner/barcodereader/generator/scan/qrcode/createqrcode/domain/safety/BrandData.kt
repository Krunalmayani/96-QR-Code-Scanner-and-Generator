package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety

/** Static, offline knowledge used by [LinkSafetyChecker]. */
internal object BrandData {
    /** Well-known brand labels (the part of the domain before the TLD). */
    val BRANDS: List<String> = listOf(
        "google", "youtube", "gmail", "paypal", "amazon", "apple", "icloud", "microsoft", "outlook",
        "facebook", "instagram", "whatsapp", "netflix", "binance", "coinbase", "kraken", "metamask",
        "chase", "wellsfargo", "bankofamerica", "citibank", "hsbc", "barclays", "santander", "americanexpress",
        "mastercard", "dhl", "fedex", "ups", "usps", "linkedin", "twitter", "x", "dropbox", "adobe", "ebay",
        "alibaba", "aliexpress", "flipkart", "paytm", "phonepe", "sbi", "hdfc", "icici", "axisbank", "telegram",
        "spotify", "steam", "github", "tiktok", "snapchat", "pinterest", "reddit", "discord", "yahoo", "walmart",
        "venmo", "cashapp", "zelle", "robinhood", "revolut", "docusign", "zoom", "slack", "stripe", "shopify",
    )

    /** Labels that sit close to a brand but are real, unrelated sites (avoids false positives). */
    val BENIGN_LABELS: Set<String> = setOf(
        "apples", "applet", "apply", "ample", "stream", "phase", "paypay", "chases", "chased", "zoos", "slick", "stripes",
        "reddits", "ebays", "steamy",
    )

    /** Full registrable domains that belong to a brand but do not use its label. */
    val OFFICIAL_DOMAINS: Set<String> = setOf(
        "youtu.be", "fb.com", "fb.me", "wa.me", "t.me", "amzn.to", "amzn.com", "amazon.com", "live.com", "msn.com",
        "bing.com", "gstatic.com", "g.co", "goo.gl", "icloud-content.com", "mzstatic.com", "fbcdn.net", "cdninstagram.com",
        "licdn.com", "paypalobjects.com", "scdn.co", "redd.it", "redditstatic.com", "discord.gg", "discordapp.com",
        "steamcommunity.com", "steampowered.com", "x.com", "t.co", "ebaystatic.com", "ebayimg.com", "dhl.de",
        "ups.com", "usps.com", "fedex.com", "chase.com", "wf.com", "bofa.com", "aka.ms", "office365.com",
    )

    /** Registrable labels that legitimately start with a brand name (CDNs, APIs, apps). */
    val OFFICIAL_LABELS: Set<String> = setOf(
        "googleusercontent", "googleapis", "googlevideo", "googleadservices", "googlesyndication", "googletagmanager",
        "googleanalytics", "googlemail", "googlecloud", "googledrive", "microsoftonline", "microsoftstore", "amazonaws",
        "amazonpay", "amazonses", "githubusercontent", "githubassets", "githubapp", "dropboxusercontent", "dropboxstatic",
        "facebookmail", "adobelogin", "adobeaemcloud", "applecard", "linkedinlearning", "slackb", "stripecdn", "shopifycdn",
        "shopifycloud", "zoominfo", "twitterstat", "icloud-content", "redditmedia", "discordapp", "twimg",
    )

    /** Hosts where anyone can publish under a subdomain: a brand label there is *not* official. */
    val SHARED_HOSTING: Set<String> = setOf(
        "github.io", "gitlab.io", "blogspot.com", "herokuapp.com", "vercel.app", "netlify.app", "web.app", "firebaseapp.com",
        "pages.dev", "workers.dev", "wixsite.com", "weebly.com", "azurewebsites.net", "appspot.com", "glitch.me",
        "repl.co", "wordpress.com", "000webhostapp.com", "sites.google.com", "myshopify.com", "notion.site",
    )

    /** Public suffixes with more than one label (a small, practical subset of the Public Suffix List). */
    val MULTI_PART_SUFFIXES: Set<String> = SHARED_HOSTING + setOf(
        "co.uk", "org.uk", "ac.uk", "gov.uk", "me.uk", "ltd.uk", "com.au", "net.au", "org.au", "edu.au", "gov.au",
        "co.in", "net.in", "org.in", "firm.in", "gen.in", "ac.in", "gov.in", "co.jp", "ne.jp", "or.jp", "ac.jp",
        "com.br", "net.br", "org.br", "com.cn", "net.cn", "org.cn", "com.mx", "com.ar", "com.co", "com.pe", "com.ve",
        "co.za", "org.za", "co.nz", "net.nz", "org.nz", "com.sg", "com.hk", "com.tw", "com.tr", "co.kr", "or.kr",
        "co.id", "co.th", "com.my", "com.ph", "com.vn", "com.pk", "com.bd", "com.ng", "com.eg", "com.sa", "com.ua",
        "com.ru", "co.il", "co.ke", "com.gh", "com.np", "com.lk", "com.pl",
    )

    val SUSPICIOUS_TLDS: Set<String> = setOf(
        "zip", "mov", "xyz", "top", "tk", "ml", "ga", "cf", "gq", "click", "loan", "work", "country", "kim", "men",
        "review", "stream", "download", "racing", "win", "bid", "science", "party", "gdn", "cyou", "icu", "monster",
        "buzz", "rest", "cam", "sbs", "cfd", "bond", "quest", "accountant", "cricket", "date", "faith", "trade", "webcam",
    )

    /** Generic redirect services. Brand-owned shorteners (youtu.be, amzn.to, fb.me, ...) are deliberately absent. */
    val SHORTENERS: Set<String> = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly", "cutt.ly", "rebrand.ly", "shorturl.at",
        "tiny.cc", "rb.gy", "bl.ink", "short.io", "t2m.io", "v.gd", "tr.im", "soo.gd", "s.id", "qr.ae", "adf.ly",
        "bc.vc", "clck.ru", "shorte.st", "trib.al", "dlvr.it", "mcaf.ee", "snip.ly", "urlz.fr", "hyperurl.co", "po.st",
        "chilp.it", "u.to", "x.co", "1url.com", "bit.do", "tny.im", "ity.im", "reurl.cc", "tiny.one", "smarturl.it",
    )

    /** Schemes that can run code or read local data. */
    val DANGEROUS_SCHEMES: Set<String> = setOf("javascript", "data", "file", "intent", "vbscript", "blob", "content")
}
