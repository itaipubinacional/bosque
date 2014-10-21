//
//  Marker.h
//  geocab
//
//  Created by Henrique Lobato on 06/10/14.
//  Copyright (c) 2014 Itaipu. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Layer.h"
#import "StatusMarker.h"
#import <CoreLocation/CoreLocation.h>

@interface Marker : NSObject

@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSString *description;
@property (nonatomic) CLLocationDegrees *latitude;
@property (nonatomic) CLLocationDegrees *longitude;
@property (strong, nonatomic) Layer *layer;
@property (nonatomic) enum StatusMarker statusMarker;

@end
