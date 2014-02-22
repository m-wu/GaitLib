LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
#LOCAL_STATIC_LIBRARIES := libandroid
LOCAL_LDLIBS += -landroid
LOCAL_LDLIBS += -llog
LOCAL_MODULE    := ReadAccelerometer
LOCAL_SRC_FILES := ReadAccelerometer.c




include $(BUILD_SHARED_LIBRARY)
