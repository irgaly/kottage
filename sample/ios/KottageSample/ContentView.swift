import SwiftUI
import Kottage

struct ContentView: View {
    var body: some View {
        VStack {
            Button("Kottage", action: {
                let directory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!.absoluteString
                let kottage = Kottage(
                    name: "test",
                    directoryPath: directory,
                    environment: KottageEnvironment()
                    json: ...
                    scope: ...
                )
                // Swift からの利用はあくまで実験的なものでまだSwiftから実用的ではありません...
            })
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
