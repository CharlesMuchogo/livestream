import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
