package ediger.diarynutrition.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ediger.diarynutrition.R

// TODO: TEMPORARY
class StubFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frament_stub, container)

        view.findViewById<TextView>(R.id.position).text = arguments?.getString("position", "0")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.position).text = arguments?.getString("position", "0")
    }
}