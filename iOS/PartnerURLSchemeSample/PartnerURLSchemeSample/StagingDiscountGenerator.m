//
//  LiveDiscountGenerator.m
//  PartnerURLSchemeSample
//
//  Created by Ellen Shapiro (Vokal) on 6/3/15.
//  Copyright (c) 2015 Parche. All rights reserved.
//

#import "StagingDiscountGenerator.h"

static NSString *const StagingBaseURL = @"https://api-staging.goparche.com/";
static NSString *const DiscountRequestEndpoint = @"v1/partner/%@/create_discount/";
static NSString *const APIKey = @"kLd67mG8";
static NSString *const FakeUserID = @"qa_test@example.com";

static NSString *const PartnerUserIDKey = @"partner_user_id";
static NSString *const APISecretKey = @"api_secret";
static NSString *const DiscountCodeKey = @"discount_code";


@implementation StagingDiscountGenerator

+ (void)fireCompletionOnMainThread:(DiscountGenerationCompletion)completion withDiscount:(NSString *)discount andError:(NSError *)error
{
    //Send nil strings back if discount is nil.
    NSString *apiKey = discount ? APIKey : nil;
    NSString *userID = discount ? FakeUserID : nil;
    
    if ([NSThread isMainThread]) {
        completion(discount, apiKey, userID, error);
    } else {
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            completion(discount, apiKey, userID, error);
        }];
    }
}

+ (void)requestStagingDiscountWithCompletion:(DiscountGenerationCompletion)completion
{
    //Setup the main bit of the request
    NSURL *url = [NSURL URLWithString:[StagingBaseURL stringByAppendingFormat:DiscountRequestEndpoint, APIKey]];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    
    //Setup JSON send/accept
    NSString *jsonType = @"application/json";
    [request setValue:jsonType forHTTPHeaderField:@"Accept"];
    [request setValue:jsonType forHTTPHeaderField:@"Content-Type"];
    
    
    //Add json data
    NSDictionary *requestParameters = @{
                                        PartnerUserIDKey: FakeUserID,
                                        APISecretKey: @"LSnRMHhNqMsqvNekAG3M8qDjnRMfuBD8xGaLVX5BeyJCyUB4",
                                        };
 

    NSError *jsonError;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:requestParameters options:NSJSONWritingPrettyPrinted error:&jsonError];
    if (jsonError) {
        [self fireCompletionOnMainThread:completion
                            withDiscount:nil
                                andError:jsonError];
        //Bail out since if the json data is crap, there's nothing more to do here.
        return;
    }

    request.HTTPMethod = @"POST";
    request.HTTPBody = jsonData;

    //Actually request data.
    NSURLSessionDataTask *discountTask = [[NSURLSession sharedSession] uploadTaskWithRequest:request
                                                                                    fromData:jsonData
                                                                           completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error) {
            completion(nil, nil, nil, error);
        } else {
            NSError *incomingJSONError;
            NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data
                                                                 options:0
                                                                   error:&incomingJSONError];
            if (incomingJSONError) {
                [self fireCompletionOnMainThread:completion
                                    withDiscount:nil
                                        andError:incomingJSONError];
                return;
            }
            
            NSString *discountCode = json[DiscountCodeKey];
            if (discountCode) {
                [self fireCompletionOnMainThread:completion
                                    withDiscount:discountCode
                                        andError:nil];
            } else {
                //Note that description should be localized in a real app.
                NSError *noDiscountError = [NSError errorWithDomain:@"com.parchemobile.PartnerURLSchemeSample"
                                                               code:666
                                                           userInfo:@{ NSLocalizedDescriptionKey : @"Oops, no discount code but also no error!"}];
                [self fireCompletionOnMainThread:completion
                                    withDiscount:nil
                                        andError:noDiscountError];
            }
        }
    }];
    
    [discountTask resume];
}

@end
