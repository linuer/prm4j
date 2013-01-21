/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing.realtime;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import prm4j.api.Event;
import prm4j.api.MatchHandler;
import prm4j.indexing.BaseMonitor;

public class ParametricMonitorLogger {

    protected final BindingStore bindingStore;
    protected final NodeStore nodeStore;
    protected final NodeManager nodeManager;

    private long timestamp;

    public ParametricMonitorLogger(BindingStore bindingStore, NodeStore nodeStore, NodeManager nodeManager) {
	super();
	this.bindingStore = bindingStore;
	this.nodeStore = nodeStore;
	this.nodeManager = nodeManager;
	memStats = new SummaryStatistics();
	logMemoryConsumption();
    }


    private final Logger logger = getFileLogger("logs/prm4j-stats.log");

    private SummaryStatistics memStats;

    private String experimentName = getSystemProperty("prm4j.experimentName", "");

    public void log(long timestamp, Event event) {
	this.timestamp = timestamp;
	if (timestamp % 100 == 0) {
	    logMemoryConsumption();
	}
    }

    private void logMemoryConsumption() {
	double memoryConsumption = (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024) - ((double) (Runtime
		.getRuntime().freeMemory() / 1024) / 1024));
	// filter NaNs
	if (memoryConsumption != Double.NaN) {
	    memStats.addValue(memoryConsumption);
	}
    }

    public void reset() {
	logMemoryConsumption();
	logger.log(Level.INFO, String.format(
		"%s EVENTS (totalCount) %d",
		experimentName, timestamp));
	logger.log(Level.INFO, String.format(
		"%s MATCHES (totalCount) %d",
		experimentName, MatchHandler.getMatchCount()));
	logger.log(Level.INFO,
		String.format("%s MEMORY (mean/max) %f %f", experimentName, memStats.getMean(), memStats.getMax()));
	logger.log(
		Level.INFO,
		String.format("%s BINDINGS (created/collected/stored) %d %d %d", experimentName,
			bindingStore.getCreatedBindingsCount(), bindingStore.getCollectedBindingsCount(),
			bindingStore.size()));
	logger.log(Level.INFO, String.format("%s NODES (created) %d", experimentName, nodeManager.getCreatedCount()));
	logger.log(Level.INFO, String.format("%s MONITORS (created/updated/orphaned/collected) %d %d %d %d",
		experimentName, BaseMonitor.getCreatedMonitorsCount(), BaseMonitor.getUpdateddMonitorsCount(),
		nodeManager.getOrphanedMonitorsCount(), nodeManager.getCollectedMonitorsCount()));
	memStats.clear();
    }

    private static String getSystemProperty(String key, String defaultValue) {
	final String value = System.getProperty(key);
	return value != null ? value : defaultValue;
    }

    /**
     * A simple file logger which outputs only the message.
     *
     * @param fileName
     *            path to the output file
     * @return the logger
     */
    private static Logger getFileLogger(String fileName) {
	final Logger logger = Logger.getLogger(fileName);
	try {
	    logger.setUseParentHandlers(false);
	    Handler handler = new FileHandler(fileName, true);
	    handler.setFormatter(new Formatter() {
		@Override
		public String format(LogRecord record) {
		    return record.getMessage() + "\n";
		}
	    });
	    logger.addHandler(handler);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
	return logger;
    }

}