Ext.ns('GDP');

GDP.Animator = Ext.extend(Ext.util.Observable, {
	timeInterval : 1000,
	indexInterval : 1,
	currentIndex : -1,
	currentTimeoutID : undefined,
	minIndex : 0,
	maxIndex : -1,
	constructor : function(config) {
		if (!config) config = {};
		
		this.timeInterval = config.timeInterval || this.timeInterval;
		this.indexInterval = config.indexInterval || this.indexInterval;
		
		config = Ext.apply({
			
		}, config);
		
		GDP.Animator.superclass.constructor.call(this, config);
		
		this.addEvents(
		/**
		 * @event timedchange
		 * Fires when the animator has determined it should change the timestep
		 * @param {String} time in ISO8601
		 */
			"timedchange"
		);
	},
	startAnimation : function(index) {
		this.resetIndex(index);
		this.continueAnimation();
	},
	continueAnimation : function() {
		if (this.minIndex > this.maxIndex) return; 
		
		var nextIndex = this.currentIndex + this.indexInterval;
		if (nextIndex > this.maxIndex) {
			nextIndex = this.resetIndex() + this.indexInterval;
		}
		
		this.currentIndex = nextIndex;
		
		this.fireEvent('timedchange', this.currentIndex);
		this.currentTimeoutID = this.continueAnimation.defer(this.timeInterval, this);
	},
	stopAnimation : function() {
		if (this.currentTimeoutID) {
			clearTimeout(this.currentTimeoutID);
		}
	},
	setMinIndex : function(min) {
		this.minIndex = min;
	},
	resetIndex : function(index) {
		var reqIndex = index || this.minIndex;
		var min = reqIndex - this.indexInterval;
		this.currentIndex = min;
		return min;
	},
	setMaxIndex : function(max) {
		this.maxIndex = max;
	}
});