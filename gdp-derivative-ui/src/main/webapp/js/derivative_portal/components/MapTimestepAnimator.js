Ext.ns('GDP');

GDP.MapTimestepAnimator = Ext.extend(Ext.util.Observable, {
	baseMap : undefined,
	store : undefined,
	currentIndex : -1,
	currentTimeoutID : undefined,
	constructor : function(config) {
		if (!config) config = {};
		
		this.baseMap = config.baseMap;
		this.store = config.store;
		
		config = Ext.apply({
			
		}, config);
		
		GDP.MapTimestepAnimator.superclass.constructor.call(this, config);
	},
	startAnimation : function() {
		this.currentIndex = -1;
		this.continueAnimation();
	},
	continueAnimation : function() {
		this.currentIndex++;
		if (this.currentIndex >= this.store.getCount()) this.currentIndex = 0;
		
		this.baseMap.changeTimestep(this.store.getAt(this.currentIndex).get('time'));
		this.currentTimeoutID = this.continueAnimation.defer(1000, this);
	},
	stopAnimation : function() {
		if (this.currentTimeoutID) {
			clearTimeout(this.currentTimeoutID);
		}
	}
});