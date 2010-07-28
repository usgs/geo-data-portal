#!/bin/bash

# Change this to where log, err, and touch files are moved
SCRIPT_PATH=/opt/tomcat/npvu_scripts
LOGFILE=$SCRIPT_PATH/lftp.log
DATEFILE=$SCRIPT_PATH/last_access.touch
ERRFILE=$SCRIPT_PATH/lftp.err.`date +%Y%m%d%H%M%S`

# Change these if source or destination changes
SOURCE=/npvu/rfcqpe/mosaic
TARGET=/mnt/thredds/misc/npvu/realtime
ARCHIVE=/mnt/thredds/misc/npvu/archive

# Edit this if emails should go to someone else
EMAIL_ADDR='dblodgett@usgs.gov'

now=$(date)

email() {
	/usr/bin/mailx -s "Problem with NPVU ingest" $EMAIL_ADDR <<END
		echo "Something went wrong with the download from NPVU.  Read $ERRFILE and lftp.log to get more information.  Attached is a tail of the lftp log and the STDERR."
		echo
		echo
		echo $1
		echo
		echo
		cat $ERRFILE
END
}

cleanup_realtime() {
	seven_days_ago=$(date -u --date='7 days ago' +%Y%m%d)
	ls $TARGET | while read dir; do
		rm $TARGET/$dir/.nfs* > /dev/null
		if [[ $dir -lt $seven_days_ago ]]; then
			if [[ $(ls -A $TARGET/$dir | wc -l) -eq 0 ]]; then
				rmdir $TARGET/$dir
			else
				mv $TARGET/$dir $ARCHIVE
			fi
		fi
	done
}

if [ ! -e $DATEFILE ]; then
	email "No datefile for last modified remote files exists.  Create file before continuing."
fi

# Open up ftp connection to ncep and mirror the recent changes to THREDDS
# -N $DATEFILE will get any file changed since the modtime of $DATEFILE
# If $DATEFILE goes missing, or has bad time fix it by doing
#	> touch -m --date="datetime for get" $DATEFILE
# Any errors in gets should be written to error file and emailed to Blodgett
lftp >$ERRFILE 2>&1 <<END
	open ftp://ftp.hpc.ncep.noaa.gov
	cache off
	mirror -N $DATEFILE --log=$LOGFILE --parallel=5 $SOURCE $TARGET
	exit
END
lftp_exit=$?
if [[ $lftp_exit -ne 0 ]]; then
	log_tail=$(tail -50 $LOGFILE)
	email "$log_tail" # to blodgett
	cleanup_realtime
	exit 1
else
	# Change modtime to when this script started
	touch -m --date="$now" $DATEFILE
	# Clean up error file
	rm $ERRFILE
	cleanup_realtime
	exit 0
fi
