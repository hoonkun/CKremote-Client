import UIKit
import SwiftUI
import ComposeApp


class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    func applicationDidFinishLaunching(_ application: UIApplication) {
        AppDelegateKotlin.shared.applicationDidFinishLaunching()
    }
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        AppDelegateKotlin.shared.applicationDidFinishLaunching(options: launchOptions)
        return true
    }
    
    func application(
        _ application: UIApplication,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        AppDelegateKotlin.shared.applicationLaunchedForShortcutItem(
            item: shortcutItem
        ) {
            completionHandler($0.boolValue)
        }
    }
    
    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }
    
}

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        if let shortcut = connectionOptions.shortcutItem {
            SceneDelegateKotlin.shared.sceneWillConnectToWithOptions(options: connectionOptions)
        }
    }
    
    func windowScene(
        _ windowScene: UIWindowScene,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        SceneDelegateKotlin.shared.windowScenePerformActionFor(
            performActionFor: shortcutItem,
            completionHandler: {
                completionHandler($0.boolValue)
            }
        )
    }
    
}
