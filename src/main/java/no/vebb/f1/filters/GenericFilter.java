package no.vebb.f1.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class GenericFilter extends Filter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (event.getLoggerName().equals("no.vebb.f1.importing.Importer")) {
			return FilterReply.DENY;
		}
		if (event.getLoggerName().equals("no.vebb.f1.importing.TableImporter")) {
			return FilterReply.DENY;
		}
		return FilterReply.NEUTRAL;
	}
	
}
