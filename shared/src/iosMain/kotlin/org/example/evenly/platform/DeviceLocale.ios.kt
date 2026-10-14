package org.example.evenly.platform

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.currentLocale

actual fun deviceCurrencyCode(): String? = NSLocale.currentLocale.objectForKey(NSLocaleCurrencyCode) as? String
