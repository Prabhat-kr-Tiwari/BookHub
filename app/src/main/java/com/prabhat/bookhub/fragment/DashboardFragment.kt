package com.prabhat.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonRequest
import com.prabhat.bookhub.R
import com.prabhat.bookhub.adapter.DashboardRecyclerAdapter
import com.prabhat.bookhub.model.Book
import com.prabhat.bookhub.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap


class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard:RecyclerView


     lateinit var layoutManager:RecyclerView.LayoutManager

   lateinit var btnCheckInternet:Button

    lateinit var progressLayout:RelativeLayout
    lateinit var  progressBar:ProgressBar
     /* private val bookList= arrayListOf(
              " P.S.I Love You",
            " The Great Gatsby",
             "Anna Karenina",
             "Madame Bovary",
                "War and peace",
                  "Lolita",
                  "Middleman's",
              "The Adventure of Huckleberry finn",
            "Mob-Dick",
          "The Lord of the Rings")*/


    lateinit var recyclerAdapter:DashboardRecyclerAdapter

    private val bookInfoList= arrayListOf<Book>()

    var ratingComparator= Comparator<Book> { book1, book2 ->
        if(book1.bookRating.compareTo(book2.bookRating,true)==0)
        {
            book1.bookName.compareTo(book2.bookName,true)
        }
        else
        {
            book1.bookRating.compareTo(book2.bookRating,true)

        }

    }


   /* private val bookInfoList= arrayListOf<Book>(
            Book("P.S.I Love You","Ceclia Ahren","Rs. 299","6.8",R.drawable.ps_ily),
            Book("The Great Gatsby","F Scot","Rs. 299","6.8",R.drawable.great_gatsby),
            Book("Anna Karenina","F Scot","Rs. 299","6.8",R.drawable.anna_kare),
            Book("Madame Bovary","F Scot","Rs. 299","6.8",R.drawable.madame),
            Book("War and peace","F Scot","Rs. 299","6.8",R.drawable.war_and_peace),
            Book("Lolita","F Scot","Rs. 299","6.8",R.drawable.lolita),
            Book("Middleman's","F Scot","Rs. 299","6.8",R.drawable.middlemarch),
            Book("The Adventure of Huckleberry finn","F Scot","Rs. 299","6.8",R.drawable.adventures_finn),
            Book("Mob-Dick","F Scot","Rs. 299","6.8",R.drawable.great_gatsby),
            Book("The Lord of the Rings","F Scot","Rs. 299","6.8",R.drawable.lord_of_rings)

    )*/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_dashboard, container, false)
        setHasOptionsMenu(true)
        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)

        btnCheckInternet=view.findViewById(R.id.btnCheckInternet)
        progressBar=view.findViewById(R.id.progressBar)
        progressLayout=view.findViewById(R.id.progressLayout)
        progressLayout.visibility=View.VISIBLE

       btnCheckInternet.setOnClickListener{
            if(ConnectionManager().checkConnectivity(activity as Context))
            {
                val dialog=AlertDialog.Builder(activity as Context)
                dialog.setTitle("Success")
                dialog.setMessage("Internet connection found")
                dialog.setPositiveButton("Ok"){
                    text,listener->
                }
                dialog.create()
                dialog.show()

            }
            else
            {
                val dialog=AlertDialog.Builder(activity as Context)
                dialog.setTitle("Error")
                dialog.setMessage("Internet connection not found")
                dialog.setPositiveButton("Ok"){
                        text,listener->
                }
                dialog.create()
                dialog.show()


            }


        }
        layoutManager=LinearLayoutManager(activity)



        val queue=Volley.newRequestQueue(activity as Context)
        val url="http://13.235.250.119/v1/book/fetch_books/"
        if(ConnectionManager().checkConnectivity(activity as Context))
        {


            val jsonObjectRequest=object :JsonObjectRequest(Request.Method.GET,url,null,Response.Listener {
                try{
                    progressLayout.visibility=View.GONE
                    val success=it.getBoolean("success")
                    if(success){
                        val data=it.getJSONArray("data")
                        for(i in 0 until data.length())
                        {
                            val bookJsonObject=data.getJSONObject(i)
                            val bookObject=Book(
                                bookJsonObject.getString("book_id"),
                                bookJsonObject.getString("name"),
                                bookJsonObject.getString("author"),
                                bookJsonObject.getString("rating"),
                                bookJsonObject.getString("price"),
                                bookJsonObject.getString("image")
                            )
                            bookInfoList.add(bookObject)
                            recyclerAdapter= DashboardRecyclerAdapter(activity as Context,bookInfoList)

                            recyclerDashboard. adapter= recyclerAdapter
                            recyclerDashboard.layoutManager = layoutManager
                            recyclerDashboard.addItemDecoration(DividerItemDecoration(recyclerDashboard.context,
                                (layoutManager as LinearLayoutManager).orientation))
                        }


                    }
                    else
                    {
                        Toast.makeText(activity as Context,"Some error occurred!!",Toast.LENGTH_SHORT).show()
                    }

                }catch(e:JSONException){
                    Toast.makeText(activity as Context,"some unexpected error occurred!!",Toast.LENGTH_LONG).show()
                }

                //println("Response is $it")

            }, Response.ErrorListener {

                //println("Error is $it")
                if(activity!=null)
                {
                    Toast.makeText(activity as Context,"volley error occurred!!",Toast.LENGTH_LONG).show()
                }

            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "58283e5522b3ae"
                    return headers


                }


            }
            queue.add(jsonObjectRequest)

        }
        else{
            val dialog=AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet connection not found")
            dialog.setPositiveButton("Open setting"){
                    text,listener->
                val settingsIntent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit"){
                    text,listener->
                ActivityCompat.finishAffinity(activity as Activity)

            }

            dialog.create()
            dialog.show()


        }



        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         inflater?.inflate(R.menu.menu_dashboard,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id=item?.itemId
        if(id==R.id.action_sort)
        {
            Collections.sort(bookInfoList,ratingComparator)
            bookInfoList.reverse()
        }
        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }



}