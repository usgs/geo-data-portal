Ext.ns('GDP');

GDP.MapTimestepAnimator = Ext.extend(Ext.util.Observable, {
	store : undefined,
	currentIndex : -1,
	currentTimeoutID : undefined,
	constructor : function(config) {
		if (!config) config = {};
		
		this.store = config.store;
		
		config = Ext.apply({
			
		}, config);
		
		GDP.MapTimestepAnimator.superclass.constructor.call(this, config);
		
		this.addEvents(
		/**
		 * @event timestepchange
		 * Fires when the animator has determined it should change the timestep
		 * @param {String} time in ISO8601
		 */
			"timestepchange"
		);
	},
	startAnimation : function() {
		this.currentIndex = -1;
		this.continueAnimation();
	},
	continueAnimation : function() {
		this.currentIndex++;
		if (this.currentIndex >= this.store.getCount()) this.currentIndex = 0;
		
		this.fireEvent('timestepchange', this.store.getAt(this.currentIndex).get('time'));
		this.currentTimeoutID = this.continueAnimation.defer(1000, this);
	},
	stopAnimation : function() {
		if (this.currentTimeoutID) {
			clearTimeout(this.currentTimeoutID);
		}
	}
});