import SwiftUI
import shared

struct ContentView: View {
    @State private var text: String = "Welcome to Palzee iOS"

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundColor(.accentColor)
            
            Text(text)
                .font(.title)
                .fontWeight(.bold)

            Button("Initialize Session") {
                let sessionManager = SessionManager()
                sessionManager.saveThemeColor(color: "emerald")
                text = "Theme Saved: \(sessionManager.getThemeColor())"
            }
            .padding()
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(10)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
