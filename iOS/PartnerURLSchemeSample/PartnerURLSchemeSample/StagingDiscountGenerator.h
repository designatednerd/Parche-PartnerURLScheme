//
//  LiveDiscountGenerator.h
//  PartnerURLSchemeSample
//
//  Created by Ellen Shapiro (Vokal) on 6/3/15.
//  Copyright (c) 2015 Parche. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void (^DiscountGenerationCompletion)(NSString *discountCode, NSString *apiKey, NSString *memberNumber, NSError *error);

/**
 * A class to facilitate QA'ing these integrations on iOS. 
 */
@interface StagingDiscountGenerator : NSObject

/**
 * Hits the staging API and requests a discount for the valet@example.com location.
 */
+ (void)requestStagingDiscountWithCompletion:(DiscountGenerationCompletion)completion;

@end
