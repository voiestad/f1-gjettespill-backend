package no.voiestad.f1.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class ImporterFilter extends Filter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (event.getLoggerName().equals("no.voiestad.f1.importing.Importer")) {
			return FilterReply.ACCEPT;
		} 
		if (event.getLoggerName().equals("no.voiestad.f1.importing.TableImporter")) {
			return FilterReply.ACCEPT;
		}
		return FilterReply.DENY;
	}
	
}
