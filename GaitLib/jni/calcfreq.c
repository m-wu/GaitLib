#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <nr/nr.h>
#include <nr/nrutil.h>
#include <jni.h>

JNIEXPORT jobjectArray JNICALL
Java_org_spin_gaitlib_util_SpectralAnalyses_fasperArray(JNIEnv* env, jobject this, jfloat hifac, jfloat ofac, jfloatArray xJava, jfloatArray yJava, jfloatArray zJava, jfloatArray tJava, jint size)
{
	/* Read in Arrays */
	jfloat *signalX = (*env)->GetFloatArrayElements(env, xJava, 0);
	jfloat *signalY = (*env)->GetFloatArrayElements(env, yJava, 0);
	jfloat *signalZ = (*env)->GetFloatArrayElements(env, zJava, 0);
	jfloat *jTime = (*env)->GetFloatArrayElements(env, tJava, 0);
	
	unsigned long j=0,MP,jmax,n,nout;
	float ans,prob,*px,*py,*x,*y,time0;
	int i;
	
	MP = 90*size;
	x=vector(1,size);
	y=vector(1,size);
	px=vector(1,MP);
	py=vector(1,MP);
	for(n=0;n<size;n++) {
		x[++j]=jTime[n] - jTime[0]; /* It is important that the time set start at 0, or else the spectral range is incorrect */
		y[j]=sqrt(pow(signalX[n],2) + pow(signalY[n],2) + pow(signalZ[n],2));
	}
	
	fasper(x,y,j,ofac,hifac,px,py,MP,&nout,&jmax,&prob);
	
	/* Create array to return freqs (px) and power (py) */
	jobjectArray result;
	jclass floatArrCls = (*env)->FindClass(env, "[F");
	if (floatArrCls == NULL) {
		return NULL; /* exception thrown */
	}
	result = (*env)->NewObjectArray(env, nout, floatArrCls, NULL); /* create object array size nout */
	if(result == NULL) {
		return NULL;
	}
	
	/* Fill array */
	for (i=0; i<nout; i++) {
		int size = 2;
		
		if(jmax == i+1) {
			size = 3;
		}
		
		jfloat tmp[size];  // make sure it is large enough! 
		
		jfloatArray farr = (*env)->NewFloatArray(env, size);
		if (farr == NULL) {
			return NULL; // out of memory error thrown
		}
		
		tmp[0] = px[i+1];
		tmp[1] = py[i+1];
		
		if(jmax == i+1) {
			tmp[2] = prob;
		}
		
		(*env)->SetFloatArrayRegion(env, farr, 0, size, tmp);
		
		(*env)->SetObjectArrayElement(env, result, i, farr);
		(*env)->DeleteLocalRef(env, farr);
	}
	
	/* Free Memory */
	free_vector(py,1,MP);
	free_vector(px,1,MP);
	free_vector(y,1,size);
	free_vector(x,1,size);
	(*env)->ReleaseFloatArrayElements(env, xJava, signalX, 0);
	(*env)->ReleaseFloatArrayElements(env, yJava, signalY, 0);
	(*env)->ReleaseFloatArrayElements(env, zJava, signalZ, 0);
	(*env)->ReleaseFloatArrayElements(env, tJava, jTime, 0);

	return result;
}

/*
 *	Tool in progress to convert multidimensional C arrays to multidimensional Java arrays
 */
int create2DFloatArray(JNIEnv* env, float** arr, int dim1, int dim2, jobjectArray* result) {
	jclass floatArrCls = (*env)->FindClass(env, "[F");
	
	if (floatArrCls == NULL) {
		return 0; /* exception thrown */
	}
	
	result = (*env)->NewObjectArray(env, dim1, floatArrCls, NULL);
	
	if(result == NULL) {
		return 0; /* exception thrown */
	}
	
	/* Fill array */
	int i,j;
	for (i=0; i<dim1; i++)
	{
		jfloat tmp[dim2];  // make sure it is large enough! 
		jfloatArray farr = (*env)->NewFloatArray(env, dim2);
		if (farr == NULL) {
			return 0; /* exception thrown */
		}
		
		for(j=0;j<dim2;j++) {
			tmp[j]=arr[i][j];
		}
		
		(*env)->SetFloatArrayRegion(env, farr, 0, dim2, tmp); //fill farr with tmp values
		(*env)->SetObjectArrayElement(env, result, i, farr); //place far in the array of arrays
		(*env)->DeleteLocalRef(env, farr);
	}
}